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
import io.qameta.allure.option.ConfigOptions;
import io.qameta.allure.option.ReportLanguageOptions;
import io.qameta.allure.option.ReportNameOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class CommandsTest {

    @Test
    void shouldNotFailWhenListPluginsWithoutConfig(@TempDir final Path home) {
        final Commands commands = new Commands(home);
        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("some-profile");
        final ExitCode exitCode = commands.listPlugins(options);

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    @Test
    void shouldFailIfDirectoryExists(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportPath = Files.createDirectories(temp.resolve("report"));
        Files.createTempFile(reportPath, "some", ".txt");
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.generate(
                reportPath, Collections.emptyList(), false, false,
                new ConfigOptions(), new ReportNameOptions(), new ReportLanguageOptions()
        );

        assertThat(exitCode)
                .isEqualTo(ExitCode.GENERIC_ERROR);
    }

    @Test
    void shouldListPlugins(@TempDir final Path home) throws Exception {
        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.listPlugins(options);

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    @Test
    void shouldLoadConfig(@TempDir final Path home) throws Exception {
        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");

        final Commands commands = new Commands(home);
        final CommandlineConfig config = commands.getConfig(options);
        assertThat(config)
                .isNotNull();

        assertThat(config.getPlugins())
                .hasSize(3)
                .containsExactly("a", "b", "c");
    }

    @Test
    void shouldAllowEmptyReportDirectory(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));

        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");
        final Path reportPath = Files.createDirectories(temp.resolve("report"));
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.generate(
                reportPath,
                Collections.emptyList(), false, false, options,
                new ReportNameOptions(), new ReportLanguageOptions()
        );

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    @Test
    void shouldServeRegularFileWhenReportDirectoryIsNotNormalized(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        Files.writeString(reportDirectory.resolve("app.js"), "console.log('report');");

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory.resolve("."));
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

    @Test
    void shouldServeImageDiffAttachment(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final String payload = "{\"expected\":\"data:image/png;base64,AAA=\"}";
        Files.writeString(reportDirectory.resolve("step-diff.imagediff"), payload);

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory);
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

    @Test
    void shouldServeUnknownFileAsOctetStream(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final String payload = "raw attachment";
        Files.writeString(reportDirectory.resolve("attachment.foobar"), payload);

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory);
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

    @Test
    void shouldServeDirectoryIndexWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path nestedDirectory = Files.createDirectories(reportDirectory.resolve("nested"));
        Files.writeString(nestedDirectory.resolve("index.html"), "nested report");

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory);
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

    @Test
    void shouldReturnNotFoundForInvalidRequestPathWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        Files.writeString(reportDirectory.resolve("index.html"), "report");

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory);
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

    @Test
    void shouldDetectResolvedPathOutsideNormalizedReportDirectory(@TempDir final Path temp) throws Exception {
        final Path normalizedReportDirectory = Files.createDirectories(temp.resolve("report")).normalize();
        final Path requestedPath = normalizedReportDirectory.resolve("../outside.txt").normalize();

        assertThat(Commands.isWithinReportDirectory(normalizedReportDirectory, requestedPath))
                .isFalse();
    }

    @Test
    void shouldReturnNotFoundForMissingFileWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory);
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

    @Test
    void shouldReturnNotFoundForDirectoryWithoutServableIndexWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path nestedDirectory = Files.createDirectories(reportDirectory.resolve("nested"));
        final Path targetFile = Files.writeString(reportDirectory.resolve("target.html"), "target");
        createSymbolicLinkOrSkip(nestedDirectory.resolve("index.html"), targetFile);

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory);
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

    @Test
    void shouldReturnNotFoundForSymbolicLinkFilesWhenServingReport(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path targetFile = Files.writeString(reportDirectory.resolve("target.txt"), "target");
        createSymbolicLinkOrSkip(reportDirectory.resolve("target-link.txt"), targetFile);

        final Commands commands = new Commands(home);
        final HttpServer server = commands.setUpServer("127.0.0.1", 0, reportDirectory);
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

    private void createConfig(final Path home, final String fileName) throws IOException {
        final Path configFolder = Files.createDirectories(home.resolve("config"));
        final Path config = configFolder.resolve(fileName);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            Files.copy(Objects.requireNonNull(is), config);
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

    private String readResponse(final HttpURLConnection connection) throws IOException {
        try (InputStream response = Objects.nonNull(connection.getErrorStream())
                ? connection.getErrorStream()
                : connection.getInputStream()) {
            return new String(response.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
