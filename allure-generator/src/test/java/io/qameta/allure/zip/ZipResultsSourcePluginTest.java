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
package io.qameta.allure.zip;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipResultsSourcePluginTest {

    private Path zipFile;

    @BeforeEach
    void setUp() throws IOException {
        this.zipFile = Files.createTempFile("zip-results-source", ".zip");
    }

    @Test
    void shouldRunOtherPlugins() throws Exception {
        Set<TestResult> testResults = zipAndRunPlugin(
                new ResourceToZipEntry("allure2/simple-testcase.json", "simple-testcase-result.json"),
                new ResourceToZipEntry("allure2/first-testgroup.json", "first-testgroup-container.json"),
                new ResourceToZipEntry("allure2/second-testgroup.json", "second-testgroup-container.json")
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .flatExtracting(TestResult::getBeforeStages)
                .hasSize(2)
                .extracting(StageResult::getName)
                .containsExactlyInAnyOrder("mockAuthorization", "loadTestConfiguration");
    }

    @Test
    void shouldIgnoreEmptyZipFile() throws Exception {
        Set<TestResult> testResults = zipAndRunPlugin().getResults();

        assertThat(testResults).isEmpty();
    }

    @Test
    void shouldIgnoreNonZipFile() throws Exception {
        zipFile = Files.createTempDirectory("regular-dir");

        Set<TestResult> testResults = runPlugin().getResults();

        assertThat(testResults).isEmpty();
    }

    private LaunchResults zipAndRunPlugin(ResourceToZipEntry... strings) throws IOException {
        try (ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (ResourceToZipEntry file : strings) {
                copyFile(zout, file);
            }
        }
        return runPlugin();
    }

    private LaunchResults runPlugin() {
        ZipResultsSourcePlugin reader = new ZipResultsSourcePlugin();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
        reader.readResults(configuration, resultsVisitor, zipFile);
        return resultsVisitor.getLaunchResults();
    }

    private void copyFile(ZipOutputStream zipFile, ResourceToZipEntry resource) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource.getResourceName())) {
            ZipEntry zipEntry = new ZipEntry(resource.getZipEntry());
            zipFile.putNextEntry(zipEntry);
            IOUtils.copy(Objects.requireNonNull(is), zipFile);
            zipFile.closeEntry();
        }
    }

    private static String generateTestResultName() {
        return UUID.randomUUID() + "-result.json";
    }

    private static String generateTestResultContainerName() {
        return UUID.randomUUID() + "-container.json";
    }

    static class ResourceToZipEntry {
        private final String resourceName;
        private final String zipEntry;

        public ResourceToZipEntry(String resourceName, String zipEntry) {
            this.resourceName = resourceName;
            this.zipEntry = zipEntry;
        }

        public String getResourceName() {
            return resourceName;
        }

        public String getZipEntry() {
            return zipEntry;
        }
    }
}
