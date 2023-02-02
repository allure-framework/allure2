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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.qameta.allure.testdata.TestData.allure1data;
import static io.qameta.allure.testdata.TestData.unpackFile;
import static org.assertj.core.api.Assertions.assertThat;

class ReportGeneratorTest {

    private static Path output;

    @BeforeAll
    static void setUp(@TempDir final Path temp) throws Exception {
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);
        output = temp.resolve("report");
        final Path resultsDirectory = Files.createDirectories(temp.resolve("results"));
        allure1data().forEach(resource -> unpackFile(
                "allure1data/" + resource,
                resultsDirectory.resolve(resource)
        ));
        generator.generate(output, resultsDirectory);
    }

    @Test
    void shouldGenerateIndexHtml() {
        assertThat(output.resolve("index.html"))
                .isRegularFile();
    }

    @Test
    void shouldWriteReportStatic() {
        assertThat(output.resolve("app.js"))
                .isRegularFile();
        assertThat(output.resolve("styles.css"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateCategoriesJson() {
        assertThat(output.resolve("data/categories.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateXunitJson() {
        assertThat(output.resolve("data/suites.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateTimelineJson() {
        assertThat(output.resolve("data/timeline.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetCategoriesJson() {
        assertThat(output.resolve("widgets/categories.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetCategoriesTrendJson() {
        assertThat(output.resolve("widgets/categories-trend.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetDurationJson() {
        assertThat(output.resolve("widgets/duration.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetDurationTrendJson() {
        assertThat(output.resolve("widgets/duration-trend.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetRetryTrendJson() {
        assertThat(output.resolve("widgets/retry-trend.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetEnvironmentJson() {
        assertThat(output.resolve("widgets/environment.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetExecutorsJson() {
        assertThat(output.resolve("widgets/executors.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetHistoryTrendJson() {
        assertThat(output.resolve("widgets/history-trend.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetLaunchJson() {
        assertThat(output.resolve("widgets/launch.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetSeverityJson() {
        assertThat(output.resolve("widgets/severity.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetStatusJson() {
        assertThat(output.resolve("widgets/status-chart.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetSuitesJson() {
        assertThat(output.resolve("widgets/suites.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateWidgetSummaryJson() {
        assertThat(output.resolve("widgets/summary.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateAttachments() throws Exception {
        final Path attachmentsFolder = output.resolve("data/attachments");
        assertThat(attachmentsFolder)
                .isDirectory();
        assertThat(Files.list(attachmentsFolder))
                .hasSize(13);
    }

    @Test
    void shouldGenerateTestCases() throws Exception {
        final Path testCasesFolder = output.resolve("data/test-cases");
        assertThat(testCasesFolder)
                .isDirectory();
        assertThat(Files.list(testCasesFolder))
                .hasSize(20);
    }

    @Test
    void shouldGenerateHistory() {
        assertThat(output.resolve("history/history.json"))
                .isRegularFile();
    }

    @Test
    void shouldGenerateMail() {
        assertThat(output.resolve("export/mail.html"))
                .isRegularFile();
    }
}
