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

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the local-only Allure report preview server.
 */
class LocalReportServerTest {

    /**
     * Verifies the report server normalizes report-directory paths before serving files.
     * The test checks a regular JavaScript file is served with the expected content type and body.
     */
    @Description
    @Test
    void shouldServeRegularFileWhenReportDirectoryIsNotNormalized(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        Files.writeString(reportDirectory.resolve("app.js"), "console.log('report');");

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory.resolve("."));
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection fileRequest = openConnection(port, "/app.js");
            assertThat(fileRequest.getResponseCode()).isEqualTo(200);
            assertThat(fileRequest.getHeaderField("Content-Type"))
                    .isEqualTo("application/javascript");
            assertThat(readResponse(fileRequest)).isEqualTo("console.log('report');");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies the report server refuses to bind to a remote-facing host.
     * The test checks allure serve stays a local preview command by default.
     */
    @Description
    @Test
    void shouldRejectRemoteHostWhenSettingUpReportServer(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));

        assertThatThrownBy(() -> LocalReportServer.setUp("0.0.0.0", 0, reportDirectory))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("local report preview only");
    }

    /**
     * Verifies the report server rejects requests proxied through a non-local host name.
     * The test checks a hostile Host header receives a forbidden response before file serving.
     */
    @Description
    @Test
    void shouldRejectNonLocalHostHeaderWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        Files.writeString(reportDirectory.resolve("index.html"), "report");

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final String response = readRawHttpResponse(port, "reports.example.com", "/index.html");
            assertThat(response)
                    .contains("403 Forbidden")
                    .contains("Content-security-policy");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies the local report server sends browser hardening headers.
     * The test checks regular report files receive CSP, nosniff, referrer, cache, frame, and permission policies.
     */
    @Description
    @Test
    void shouldSetSecurityHeadersWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        Files.writeString(reportDirectory.resolve("index.html"), "report");

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection reportRequest = openConnection(port, "/index.html");
            assertThat(reportRequest.getResponseCode()).isEqualTo(200);
            assertThat(reportRequest.getHeaderField("Content-Security-Policy"))
                    .contains("default-src 'self'")
                    .contains("frame-ancestors 'none'")
                    .contains("script-src 'self' 'unsafe-inline'");
            assertThat(reportRequest.getHeaderField("X-Content-Type-Options")).isEqualTo("nosniff");
            assertThat(reportRequest.getHeaderField("Referrer-Policy")).isEqualTo("no-referrer");
            assertThat(reportRequest.getHeaderField("Cache-Control")).isEqualTo("no-store");
            assertThat(reportRequest.getHeaderField("X-Frame-Options")).isEqualTo("DENY");
            assertThat(reportRequest.getHeaderField("Permissions-Policy"))
                    .contains("camera=()")
                    .contains("microphone=()")
                    .contains("clipboard-read=()");
            assertThat(reportRequest.getHeaderField("Content-Disposition")).isNull();
            assertThat(readResponse(reportRequest)).isEqualTo("report");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies HTML report attachments can still be previewed without same-origin script privileges.
     * The test checks HTML attachments stay inline while receiving a restrictive sandbox CSP.
     */
    @Description
    @Test
    void shouldSandboxHtmlAttachmentsWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path attachments = Files.createDirectories(reportDirectory.resolve("data").resolve("attachments"));
        Files.writeString(attachments.resolve("payload.html"), "<script>alert(1)</script>");

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection attachmentRequest = openConnection(port, "/data/attachments/payload.html");
            assertThat(attachmentRequest.getResponseCode()).isEqualTo(200);
            assertThat(attachmentRequest.getHeaderField("Content-Type")).isEqualTo("text/html");
            assertThat(attachmentRequest.getHeaderField("Content-Disposition")).isNull();
            assertThat(attachmentRequest.getHeaderField("Content-Security-Policy"))
                    .contains("sandbox")
                    .contains("default-src 'none'")
                    .contains("frame-ancestors 'self'")
                    .contains("style-src 'unsafe-inline'");
            assertThat(attachmentRequest.getHeaderField("X-Frame-Options")).isEqualTo("SAMEORIGIN");
            assertThat(attachmentRequest.getHeaderField("X-Content-Type-Options")).isEqualTo("nosniff");
            assertThat(readResponse(attachmentRequest)).isEqualTo("<script>alert(1)</script>");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies risky non-preview attachments are served as downloads by allure serve.
     * The test checks unknown attachment types receive download disposition and a sandbox CSP.
     */
    @Description
    @Test
    void shouldForceUnknownAttachmentsToDownloadWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path attachments = Files.createDirectories(reportDirectory.resolve("data").resolve("attachments"));
        Files.writeString(attachments.resolve("payload.bin"), "raw payload");

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection attachmentRequest = openConnection(port, "/data/attachments/payload.bin");
            assertThat(attachmentRequest.getResponseCode()).isEqualTo(200);
            assertThat(attachmentRequest.getHeaderField("Content-Type")).isEqualTo("application/octet-stream");
            assertThat(attachmentRequest.getHeaderField("Content-Disposition")).isEqualTo("attachment");
            assertThat(attachmentRequest.getHeaderField("Content-Security-Policy"))
                    .isEqualTo("sandbox; default-src 'none'");
            assertThat(attachmentRequest.getHeaderField("X-Content-Type-Options")).isEqualTo("nosniff");
            assertThat(readResponse(attachmentRequest)).isEqualTo("raw payload");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies the report server preserves the Allure image-diff attachment content type.
     * The test checks an imagediff file is served with the expected media type and body.
     */
    @Description
    @Test
    void shouldServeImageDiffAttachment(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final String payload = "{\"expected\":\"data:image/png;base64,AAA=\"}";
        Files.writeString(reportDirectory.resolve("step-diff.imagediff"), payload);

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection fileRequest = openConnection(port, "/step-diff.imagediff");
            assertThat(fileRequest.getResponseCode()).isEqualTo(200);
            assertThat(fileRequest.getHeaderField("Content-Type"))
                    .isEqualTo("application/vnd.allure.image.diff");
            assertThat(readResponse(fileRequest)).isEqualTo(payload);
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies the report server preserves the Allure HTTP Exchange content type.
     * The test checks a .httpexchange file is served with the expected media type and body.
     */
    @Description
    @Test
    void shouldServeHttpAttachment(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final String payload = "{\"schemaVersion\":1,"
                + "\"request\":{\"method\":\"GET\",\"url\":\"https://example.org\"}}";
        Files.writeString(reportDirectory.resolve("api-call.httpexchange"), payload);

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection fileRequest = openConnection(port, "/api-call.httpexchange");
            assertThat(fileRequest.getResponseCode()).isEqualTo(200);
            assertThat(fileRequest.getHeaderField("Content-Type"))
                    .isEqualTo("application/vnd.allure.http+json");
            assertThat(readResponse(fileRequest)).isEqualTo(payload);
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies unknown report files are served as generic binary attachments.
     * The test checks an unrecognized extension receives octet-stream content type and the original body.
     */
    @Description
    @Test
    void shouldServeUnknownFileAsOctetStream(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final String payload = "raw attachment";
        Files.writeString(reportDirectory.resolve("attachment.foobar"), payload);

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection fileRequest = openConnection(port, "/attachment.foobar");
            assertThat(fileRequest.getResponseCode()).isEqualTo(200);
            assertThat(fileRequest.getHeaderField("Content-Type"))
                    .isEqualTo("application/octet-stream");
            assertThat(readResponse(fileRequest)).isEqualTo(payload);
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies directory requests serve nested index files from inside the report directory.
     * The test checks the nested index response has a success status and expected body.
     */
    @Description
    @Test
    void shouldServeDirectoryIndexWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path nestedDirectory = Files.createDirectories(reportDirectory.resolve("nested"));
        Files.writeString(nestedDirectory.resolve("index.html"), "nested report");

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection directoryRequest = openConnection(port, "/nested/");
            assertThat(directoryRequest.getResponseCode()).isEqualTo(200);
            assertThat(readResponse(directoryRequest)).isEqualTo("nested report");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies encoded traversal attempts are rejected by the report server.
     * The test checks a normalized outside path returns a 404 response.
     */
    @Description
    @Test
    void shouldReturnNotFoundForInvalidRequestPathWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        Files.writeString(reportDirectory.resolve("index.html"), "report");

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection invalidRequest = openConnection(
                    port, "/nested/%2e%2e/index.html"
            );
            assertThat(invalidRequest.getResponseCode()).isEqualTo(404);
            assertThat(readResponse(invalidRequest)).isEqualTo("404 Not Found");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies explicit path-boundary checks reject paths outside the normalized report directory.
     * The test checks a normalized parent traversal target is not considered inside the report directory.
     */
    @Description
    @Test
    void shouldDetectResolvedPathOutsideNormalizedReportDirectory(@TempDir final Path temp) throws Exception {
        final Path normalizedReportDirectory = Files.createDirectories(temp.resolve("report")).normalize();
        final Path requestedPath = normalizedReportDirectory.resolve("../outside.txt").normalize();

        assertThat(LocalReportServer.isWithinReportDirectory(normalizedReportDirectory, requestedPath))
                .isFalse();
    }

    /**
     * Verifies missing report files return a not-found response.
     * The test checks the server returns both 404 status and the standard not-found body.
     */
    @Description
    @Test
    void shouldReturnNotFoundForMissingFileWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection missingFileRequest = openConnection(port, "/missing.js");
            assertThat(missingFileRequest.getResponseCode()).isEqualTo(404);
            assertThat(readResponse(missingFileRequest)).isEqualTo("404 Not Found");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies directory indexes that resolve through symbolic links are not served.
     * The test checks a symlinked nested index returns a not-found response.
     */
    @Description
    @Test
    void shouldReturnNotFoundForDirectoryWithoutServableIndexWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path nestedDirectory = Files.createDirectories(reportDirectory.resolve("nested"));
        final Path targetFile = Files.writeString(reportDirectory.resolve("target.html"), "target");
        createSymbolicLinkOrSkip(nestedDirectory.resolve("index.html"), targetFile);

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection indexRequest = openConnection(port, "/nested/");
            assertThat(indexRequest.getResponseCode()).isEqualTo(404);
            assertThat(readResponse(indexRequest)).isEqualTo("404 Not Found");
        } finally {
            server.stop(0);
        }
    }

    /**
     * Verifies symlinked files inside the report directory are not served.
     * The test checks a symlinked attachment path returns a not-found response.
     */
    @Description
    @Test
    void shouldReturnNotFoundForSymbolicLinkFilesWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path targetFile = Files.writeString(reportDirectory.resolve("target.txt"), "target");
        createSymbolicLinkOrSkip(reportDirectory.resolve("target-link.txt"), targetFile);

        final HttpServer server = LocalReportServer.setUp("127.0.0.1", 0, reportDirectory);
        server.start();

        try {
            final int port = server.getAddress().getPort();

            final HttpURLConnection symlinkedFileRequest = openConnection(port, "/target-link.txt");
            assertThat(symlinkedFileRequest.getResponseCode()).isEqualTo(404);
            assertThat(readResponse(symlinkedFileRequest)).isEqualTo("404 Not Found");
        } finally {
            server.stop(0);
        }
    }

    private void createSymbolicLinkOrSkip(final Path link, final Path target) throws IOException {
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | IOException e) {
            throw new TestAbortedException("Symbolic links are not supported in this environment", e);
        }
    }

    private HttpURLConnection openConnection(final int port, final String path) throws IOException {
        return (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
    }

    @Step("Read raw HTTP response")
    private String readRawHttpResponse(final int port, final String hostHeader, final String path) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            final String request = String.format(
                    "GET %s HTTP/1.1\r%nHost: %s\r%nConnection: close\r%n\r%n",
                    path,
                    hostHeader
            );
            socket.getOutputStream().write(request.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();

            final String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            Allure.addAttachment("raw-http-response.txt", "text/plain", response, ".txt");
            return response;
        }
    }

    @Step("Read HTTP response")
    private String readResponse(final HttpURLConnection connection) throws IOException {
        try (InputStream response = Objects.nonNull(connection.getErrorStream())
                ? connection.getErrorStream()
                : connection.getInputStream()) {
            final String body = new String(response.readAllBytes(), StandardCharsets.UTF_8);
            Allure.addAttachment(
                    "http-response.txt",
                    "text/plain",
                    String.format(
                            "url=%s%nstatus=%s%ncontentType=%s%nbody=%s%n",
                            connection.getURL(),
                            connection.getResponseCode(),
                            connection.getHeaderField("Content-Type"),
                            body
                    ),
                    ".txt"
            );
            return body;
        }
    }
}
