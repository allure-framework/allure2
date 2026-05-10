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
package io.qameta.allure.core;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Description;
import io.qameta.allure.ReportStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class ReportWebGeneratorTest {

    /**
     * Verifies referencing hashed directory assets for web report generation.
     */
    @Description
    @Test
    void shouldReferenceHashedDirectoryAssets(@TempDir final Path tempDirectory) {
        final Configuration configuration = ConfigurationBuilder.empty().build();

        generateReport(configuration, new FileSystemReportStorage(tempDirectory), tempDirectory);

        final Path indexHtml = tempDirectory.resolve("index.html");

        assertThat(indexHtml)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .contains("<script src=\"assets/")
                .contains("assets/")
                .doesNotContain("type=\"module\"")
                .doesNotContain("type=\"importmap\"")
                .doesNotContain("app.js")
                .doesNotContain("styles.css");
    }

    /**
     * Verifies disabling analytics for web report generation.
     */
    @Description
    @SetEnvironmentVariable(
            key = "ALLURE_NO_ANALYTICS",
            value = "true"
    )
    @Test
    void shouldDisableAnalytics(@TempDir final Path tempDirectory) {
        final Configuration configuration = ConfigurationBuilder.empty().build();
        final InMemoryReportStorage reportStorage = new InMemoryReportStorage();
        generateReport(configuration, reportStorage, tempDirectory);

        final Path indexHtml = tempDirectory.resolve("index.html");

        assertThat(indexHtml)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .doesNotContain("googletagmanager")
                .doesNotContain("G-FVWC4GKEYS")
                .doesNotContain("dataLayer");
    }

    /**
     * Verifies setting language for web report generation.
     */
    @Description
    @Test
    void shouldSetLanguage(@TempDir final Path tempDirectory) {
        final Configuration configuration = ConfigurationBuilder.empty()
                .withReportLanguage("xyz")
                .build();
        final InMemoryReportStorage reportStorage = new InMemoryReportStorage();
        generateReport(configuration, reportStorage, tempDirectory);

        final Path indexHtml = tempDirectory.resolve("index.html");

        assertThat(indexHtml)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .contains("lang=\"xyz\"");
    }

    /**
     * Verifies setting default language if not provided for web report generation.
     */
    @Description
    @Test
    void shouldSetDefaultLanguageIfNotProvided(@TempDir final Path tempDirectory) {
        final Configuration configuration = ConfigurationBuilder.empty()
                .build();
        final InMemoryReportStorage reportStorage = new InMemoryReportStorage();
        generateReport(configuration, reportStorage, tempDirectory);

        final Path indexHtml = tempDirectory.resolve("index.html");

        assertThat(indexHtml)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .contains("lang=\"en\"");
    }

    /**
     * Verifies inlining hashed scripts in single file mode for web report generation.
     */
    @Description
    @Test
    void shouldInlineHashedScriptsInSingleFileMode(@TempDir final Path tempDirectory) {
        final Configuration configuration = ConfigurationBuilder.empty().build();
        final InMemoryReportStorage reportStorage = new InMemoryReportStorage();

        generateReport(configuration, reportStorage, tempDirectory);

        final Path indexHtml = tempDirectory.resolve("index.html");

        assertThat(indexHtml)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .contains("window.__allureCoreLoaded")
                .contains("data:text/javascript; charset=utf-8;base64,")
                .doesNotContain("type=\"module\"")
                .doesNotContain("type=\"importmap\"");
    }

    private void generateReport(
                                final Configuration configuration,
                                final ReportStorage reportStorage,
                                final Path outputDirectory) {
        Allure.step("Generate report web assets into " + outputDirectory, () -> {
            new ReportWebGenerator().generate(configuration, reportStorage, outputDirectory);
            final Path indexHtml = outputDirectory.resolve("index.html");
            Allure.addAttachment(
                    "Generated index.html",
                    "text/html",
                    Files.readString(indexHtml, StandardCharsets.UTF_8)
            );
        });
    }
}
