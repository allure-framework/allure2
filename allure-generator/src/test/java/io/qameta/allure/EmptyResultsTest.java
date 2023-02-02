/*
 *  Copyright 2016-2023 Qameta Software OÜ
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

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
class EmptyResultsTest {

    @Test
    void shouldAllowEmptyResultsDirectory(@TempDir final Path temp) throws Exception {
        final Path resultsDirectory = Files.createDirectories(temp.resolve("results"));
        final Path outputDirectory = Files.createDirectories(temp.resolve("report"));
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generator.generate(outputDirectory, resultsDirectory);
    }

    @Test
    void shouldAllowNonExistsResultsDirectory(@TempDir final Path temp) throws Exception {
        final Path resultsDirectory = temp.resolve("results");
        final Path outputDirectory = Files.createDirectories(temp.resolve("report"));
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generator.generate(outputDirectory, resultsDirectory);
    }

    @Test
    void shouldAllowRegularFileAsResultsDirectory(@TempDir final Path temp) throws Exception {
        final Path resultsDirectory = Files.createTempFile(temp, "a", ".txt");
        final Path outputDirectory = Files.createDirectories(temp.resolve("report"));
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generator.generate(outputDirectory, resultsDirectory);
    }
}
