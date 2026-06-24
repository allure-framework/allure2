/*
 *  Copyright 2016-2026 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.qameta.allure.DefaultResultsVisitor.probeContentType;

/**
 * Creates and configures the local-only report preview server used by command-line report viewing.
 * <p>
 * This server is intentionally scoped to ephemeral local report review from commands such as
 * {@code allure serve} and {@code allure open}. It is not a supported deployment, sharing, reverse-proxy,
 * or custom-hosting surface. Hosted reports should be generated with {@code allure generate} and served as
 * static files by a production web server such as nginx or Apache, with deployment-specific security headers
 * configured there.
 * <p>
 * The policies in this class protect the local preview surface: binding is limited to loopback hosts, request
 * {@code Host} headers are restricted to local names, report responses receive browser hardening headers, and
 * attachment routes are handled conservatively so HTML previews remain usable without giving attachment content
 * script privileges in the report origin.
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
final class LocalReportServer {

    static final String LOCAL_SERVE_MESSAGE = "`allure serve` is intended for local report preview only. "
            + "To host a report, run `allure generate <results-dir> -o <report-dir>` "
            + "and serve the generated static files with a production web server.";

    static final String HEADER_HOST = "Host";
    static final String HEADER_CONTENT_TYPE = "Content-Type";
    static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    static final String HEADER_REFERRER_POLICY = "Referrer-Policy";
    static final String HEADER_CACHE_CONTROL = "Cache-Control";
    static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    static final String HEADER_PERMISSIONS_POLICY = "Permissions-Policy";

    private static final String CURRENT_DIRECTORY = ".";
    private static final String PATH_SEPARATOR = "/";
    private static final String OPEN_SQUARE_BRACKET = "[";
    private static final String CLOSE_SQUARE_BRACKET = "]";
    private static final String CSP_BASE_URI_NONE = "base-uri 'none'; ";
    private static final String CSP_FORM_ACTION_NONE = "form-action 'none'; ";
    private static final String LOCALHOST = "localhost";
    private static final String ATTACHMENTS_REQUEST_PATH = "/data/attachments/";
    private static final Set<String> LOCAL_SERVER_HOSTS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(LOCALHOST, "127.0.0.1", "::1"))
    );
    private static final String REPORT_CONTENT_SECURITY_POLICY = "default-src 'self'; "
            + "object-src 'none'; "
            + CSP_BASE_URI_NONE
            + CSP_FORM_ACTION_NONE
            + "frame-ancestors 'none'; "
            + "img-src 'self' data: blob: https:; "
            + "media-src 'self' data: blob: https:; "
            + "font-src 'self' data: https:; "
            + "connect-src 'self'; "
            + "frame-src 'self' blob:; "
            + "worker-src 'self' blob:; "
            + "script-src 'self' 'unsafe-inline' https:; "
            + "style-src 'self' 'unsafe-inline' https:";
    private static final String ATTACHMENT_CONTENT_SECURITY_POLICY = "sandbox; default-src 'none'";
    private static final String HTML_ATTACHMENT_CONTENT_SECURITY_POLICY = "sandbox; default-src 'none'; "
            + CSP_BASE_URI_NONE
            + CSP_FORM_ACTION_NONE
            + "frame-ancestors 'self'; "
            + "img-src data: blob:; "
            + "media-src data: blob:; "
            + "style-src 'unsafe-inline'; "
            + "font-src data:";
    private static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=(), "
            + "payment=(), usb=(), serial=(), hid=(), clipboard-read=()";

    private LocalReportServer() {
        throw new IllegalStateException("Do not instantiate");
    }

    /**
     * Creates a local-only HTTP server for previewing a generated report directory.
     * <p>
     * The returned server is for local browser review only. It must not be used as an application server or as the
     * origin behind a shared report URL. Callers that need hosted reports should generate static output and delegate
     * serving, caching, TLS, access control, and deployment headers to their production web server.
     *
     * @param host the local host name or loopback address to bind
     * @param port the local port to bind
     * @param reportDirectory the generated report directory to preview
     * @return configured local preview server
     * @throws IOException if the server cannot be created, including when a non-local host is requested
     */
    static HttpServer setUp(final String host, final int port, final Path reportDirectory) throws IOException {
        final String serverHost = Objects.isNull(host) ? LOCALHOST : host;
        if (!isLocalServerHost(serverHost)) {
            throw new IOException(LOCAL_SERVE_MESSAGE);
        }
        final HttpServer server = HttpServer
                .create(new InetSocketAddress(serverHost, port), 0);
        final Path normalizedReportDirectory = reportDirectory.normalize();

        server.createContext(PATH_SEPARATOR, exchange -> {
            if (!isLocalHostHeader(exchange.getRequestHeaders().getFirst(HEADER_HOST))) {
                serveForbidden(exchange);
                return;
            }
            final String requestPath = exchange.getRequestURI().getPath();
            if (!isValidRequestPath(requestPath)) {
                serveNotFound(exchange);
                return;
            }
            final Path requestedPath = normalizedReportDirectory.resolve(CURRENT_DIRECTORY + requestPath).normalize();
            if (!isWithinReportDirectory(normalizedReportDirectory, requestedPath)) {
                serveNotFound(exchange);
                return;
            }
            if (Files.isRegularFile(requestedPath, LinkOption.NOFOLLOW_LINKS)) {
                serveFile(exchange, requestedPath, isAttachmentRequest(requestPath));
                return;
            }
            if (Files.isDirectory(requestedPath, LinkOption.NOFOLLOW_LINKS)) {
                serveIndex(exchange, requestedPath.resolve("index.html"));
                return;
            }
            serveNotFound(exchange);
        });

        return server;
    }

    static boolean isLocalServerHost(final String host) {
        return Objects.isNull(host) || LOCAL_SERVER_HOSTS.contains(normalizeHost(host));
    }

    static boolean isLocalHostHeader(final String hostHeader) {
        if (Objects.isNull(hostHeader)) {
            return false;
        }

        final String trimmed = hostHeader.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        if (trimmed.startsWith(OPEN_SQUARE_BRACKET) && trimmed.contains(CLOSE_SQUARE_BRACKET)) {
            final String host = trimmed.substring(1, trimmed.indexOf(CLOSE_SQUARE_BRACKET));
            return isLocalServerHost(host);
        }

        final int portSeparator = trimmed.indexOf(':');
        final String host = portSeparator < 0 ? trimmed : trimmed.substring(0, portSeparator);
        return isLocalServerHost(host);
    }

    static boolean isWithinReportDirectory(final Path normalizedReportDirectory,
                                           final Path requestedPath) {
        return requestedPath.startsWith(normalizedReportDirectory);
    }

    private static boolean isValidRequestPath(final String requestPath) {
        return requestPath.indexOf('\\') < 0
                && Stream.of(requestPath.split(PATH_SEPARATOR))
                        .noneMatch(segment -> CURRENT_DIRECTORY.equals(segment) || "..".equals(segment));
    }

    private static void serveIndex(final HttpExchange exchange,
                                   final Path indexFile)
            throws IOException {
        if (Files.isRegularFile(indexFile, LinkOption.NOFOLLOW_LINKS)) {
            serveFile(exchange, indexFile, false);
            return;
        }
        serveNotFound(exchange);
    }

    private static void serveFile(final HttpExchange exchange,
                                  final Path file,
                                  final boolean attachmentRequest)
            throws IOException {
        final String contentType = Optional.ofNullable(probeContentType(file))
                .orElse(DefaultResultsVisitor.APPLICATION_OCTET_STREAM);
        setSecurityHeaders(exchange);
        setAttachmentHeaders(exchange, contentType, attachmentRequest);
        exchange.getResponseHeaders().set(HEADER_CONTENT_TYPE, contentType);
        exchange.sendResponseHeaders(200, Files.size(file));
        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(file, os);
        }
    }

    private static void serveNotFound(final HttpExchange exchange) throws IOException {
        setSecurityHeaders(exchange);
        final String response = "404 Not Found";
        exchange.sendResponseHeaders(404, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void serveForbidden(final HttpExchange exchange) throws IOException {
        setSecurityHeaders(exchange);
        final String response = "403 Forbidden";
        exchange.sendResponseHeaders(403, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void setSecurityHeaders(final HttpExchange exchange) {
        exchange.getResponseHeaders().set(HEADER_CONTENT_SECURITY_POLICY, REPORT_CONTENT_SECURITY_POLICY);
        exchange.getResponseHeaders().set(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
        exchange.getResponseHeaders().set(HEADER_REFERRER_POLICY, "no-referrer");
        exchange.getResponseHeaders().set(HEADER_CACHE_CONTROL, "no-store");
        exchange.getResponseHeaders().set(HEADER_X_FRAME_OPTIONS, "DENY");
        exchange.getResponseHeaders().set(HEADER_PERMISSIONS_POLICY, PERMISSIONS_POLICY);
    }

    private static void setAttachmentHeaders(final HttpExchange exchange,
                                             final String contentType,
                                             final boolean attachmentRequest) {
        if (!attachmentRequest) {
            return;
        }
        if (isHtmlContentType(contentType)) {
            exchange.getResponseHeaders().set(HEADER_CONTENT_SECURITY_POLICY, HTML_ATTACHMENT_CONTENT_SECURITY_POLICY);
            exchange.getResponseHeaders().set(HEADER_X_FRAME_OPTIONS, "SAMEORIGIN");
            return;
        }
        if (shouldServeAsDownload(contentType)) {
            exchange.getResponseHeaders().set(HEADER_CONTENT_DISPOSITION, "attachment");
            exchange.getResponseHeaders().set(HEADER_CONTENT_SECURITY_POLICY, ATTACHMENT_CONTENT_SECURITY_POLICY);
        }
    }

    private static boolean shouldServeAsDownload(final String contentType) {
        final String normalized = normalizeContentType(contentType);
        return DefaultResultsVisitor.APPLICATION_OCTET_STREAM.equals(normalized)
                || "image/svg+xml".equals(normalized)
                || "application/pdf".equals(normalized)
                || "application/xml".equals(normalized)
                || "text/xml".equals(normalized)
                || normalized.startsWith("application/vnd.ms-")
                || normalized.startsWith("application/vnd.openxmlformats-officedocument.");
    }

    private static boolean isHtmlContentType(final String contentType) {
        final String normalized = normalizeContentType(contentType);
        return "text/html".equals(normalized) || "application/xhtml+xml".equals(normalized);
    }

    private static String normalizeContentType(final String contentType) {
        return contentType.split(";", 2)[0]
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static boolean isAttachmentRequest(final String requestPath) {
        return requestPath.startsWith(ATTACHMENTS_REQUEST_PATH);
    }

    private static String normalizeHost(final String host) {
        final String trimmed = host.trim();
        final String unbracketed = trimmed.startsWith(OPEN_SQUARE_BRACKET) && trimmed.endsWith(CLOSE_SQUARE_BRACKET)
                ? trimmed.substring(1, trimmed.length() - 1)
                : trimmed;
        return unbracketed.toLowerCase(Locale.ROOT);
    }
}
