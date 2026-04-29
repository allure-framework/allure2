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

import io.qameta.allure.core.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
class EmptyResultsTest {

    /**
     * Verifies allowing empty results directory for empty results handling.
     */
    @Description
    @Test
    void shouldAllowEmptyResultsDirectory(@TempDir final Path temp) throws Exception {
        final Path resultsDirectory = Files.createDirectories(temp.resolve("results"));
        final Path outputDirectory = Files.createDirectories(temp.resolve("report"));
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generateReport(generator, outputDirectory, resultsDirectory, "empty directory");
    }

    /**
     * Verifies allowing a missing results directory for empty results handling.
     */
    @Description
    @Test
    void shouldAllowNonExistsResultsDirectory(@TempDir final Path temp) throws Exception {
        final Path resultsDirectory = temp.resolve("results");
        final Path outputDirectory = Files.createDirectories(temp.resolve("report"));
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generateReport(generator, outputDirectory, resultsDirectory, "missing directory");
    }

    /**
     * Verifies allowing regular file as results directory for empty results handling.
     */
    @Description
    @Test
    void shouldAllowRegularFileAsResultsDirectory(@TempDir final Path temp) throws Exception {
        final Path resultsDirectory = Files.createTempFile(temp, "a", ".txt");
        final Path outputDirectory = Files.createDirectories(temp.resolve("report"));
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generateReport(generator, outputDirectory, resultsDirectory, "regular file");
    }

    private void generateReport(
            final ReportGenerator generator,
            final Path outputDirectory,
            final Path resultsDirectory,
            final String resultsDirectoryKind
    ) {
        Allure.parameter("resultsDirectoryKind", resultsDirectoryKind);
        Allure.step("Generate report when results path is " + resultsDirectoryKind, () -> {
            generator.generate(outputDirectory, resultsDirectory);
            Allure.addAttachment("Generated report files", "text/plain", listRelativeFiles(outputDirectory));
        });
    }

    private String listRelativeFiles(final Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(directory::relativize)
                    .map(Path::toString)
                    .sorted()
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
