/*
 *  Copyright 2019 Qameta Software OÜ
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
@Ignore
public class EmptyResultsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldAllowEmptyResultsDirectory() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path outputDirectory = folder.newFolder().toPath();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generator.generate(outputDirectory, resultsDirectory);
    }

    @Test
    public void shouldAllowNonExistsResultsDirectory() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath().resolve("some-dir");
        final Path outputDirectory = folder.newFolder().toPath();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generator.generate(outputDirectory, resultsDirectory);
    }

    @Test
    public void shouldAllowRegularFileAsResultsDirectory() throws Exception {
        final Path resultsDirectory = folder.newFile().toPath();
        final Path outputDirectory = folder.newFolder().toPath();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);

        generator.generate(outputDirectory, resultsDirectory);
    }
}
