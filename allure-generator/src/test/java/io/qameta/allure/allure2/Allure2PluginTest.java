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
package io.qameta.allure.allure2;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.qameta.allure.entity.Status.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class Allure2PluginTest {

    private Path directory;

    @BeforeEach
    void setUp(@TempDir final Path directory) {
        this.directory = directory;
    }

    @Test
    void shouldReadBeforesFromGroups() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .flatExtracting(TestResult::getBeforeStages)
                .hasSize(2)
                .extracting(StageResult::getName)
                .containsExactlyInAnyOrder("mockAuthorization", "loadTestConfiguration");
    }

    @Test
    void shouldReadAftersFromGroups() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .flatExtracting(TestResult::getAfterStages)
                .hasSize(2)
                .extracting(StageResult::getName)
                .containsExactlyInAnyOrder("unloadTestConfiguration", "cleanUpContext");
    }

    @Test
    void shouldExcludeDuplicatedParams() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/duplicated-params.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .flatExtracting(TestResult::getParameters)
                .extracting(Parameter::getName, Parameter::getValue)
                .containsExactlyInAnyOrder(
                        tuple("name", "value"),
                        tuple("name2", "value"),
                        tuple("name", "value2"),
                        tuple("name2", "value2")
                );
    }

    @Test
    void shouldPickUpAttachmentsForTestCase() throws IOException {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/test-sample-attachment.txt", "test-sample-attachment.txt"
        ).getResults();

        assertThat(testResults)
                .describedAs("Test case is not found")
                .hasSize(1)
                .extracting(TestResult::getTestStage)
                .flatExtracting(StageResult::getSteps)
                .describedAs("Test case should have one step")
                .hasSize(1)
                .flatExtracting(Step::getAttachments)
                .describedAs("Step should have an attachment")
                .hasSize(1)
                .extracting(Attachment::getName)
                .containsExactly("String attachment in test");
    }

    @Test
    void shouldPickUpAttachmentsForAfters() throws IOException {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/after-sample-attachment.txt", "after-sample-attachment.txt"
        ).getResults();

        assertThat(testResults)
                .describedAs("Test case is not found")
                .hasSize(1)
                .flatExtracting(TestResult::getAfterStages)
                .describedAs("Test case should have afters")
                .hasSize(2)
                .flatExtracting(StageResult::getAttachments)
                .describedAs("Second after method should have an attachment")
                .hasSize(1)
                .extracting(Attachment::getName)
                .describedAs("Attachment's name is unexpected")
                .containsExactly("String attachment in after");
    }

    @Test
    void shouldDoNotOverrideAttachmentsForGroups() throws IOException {
        Set<TestResult> testResults = process(
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/second-testgroup.json", generateTestResultContainerName(),
                "allure2/after-sample-attachment.txt", "after-sample-attachment.txt"
        ).getResults();

        assertThat(testResults)
                .describedAs("Test cases is not found")
                .hasSize(2);

        testResults.forEach(testResult -> assertThat(testResult.getAfterStages())
                .hasSize(1)
                .flatExtracting(StageResult::getAttachments)
                .hasSize(1)
                .extracting(Attachment::getName)
                .containsExactly("String attachment in after"));

    }

    @Test
    void shouldProcessEmptyStatus() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/no-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    void shouldProcessNullStatus() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/null-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    void shouldProcessInvalidStatus() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/invalid-status.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getStatus)
                .containsExactly(UNKNOWN);
    }

    @Test
    void shouldProcessNullStageTime() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/null-before-group.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1);
    }

    @Test
    void shouldAddTestResultFormatLabel() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/simple-testcase.json", generateTestResultName(),
                "allure2/first-testgroup.json", generateTestResultContainerName(),
                "allure2/second-testgroup.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .extracting(result -> result.findOneLabel(LabelName.RESULT_FORMAT))
                .extracting(Optional::get)
                .containsOnly(Allure2Plugin.ALLURE2_RESULTS_FORMAT);
    }

    @Test
    void shouldProcessParameters() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/parameters.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .flatExtracting(TestResult::getParameters)
                .extracting(
                        Parameter::getName, Parameter::getValue
                )
                .containsExactlyInAnyOrder(
                        tuple("param 3", "value 3"),
                        tuple("param 5", "value 5"),
                        tuple("param 6", "value 6"),
                        tuple("param 7", "******")
                );
    }

    @Test
    void shouldProcessStepParameters() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/step-parameters.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .extracting(TestResult::getTestStage)
                .flatExtracting(StageResult::getSteps)
                .flatExtracting(Step::getParameters)
                .extracting(
                        Parameter::getName, Parameter::getValue
                )
                .containsExactlyInAnyOrder(
                        tuple("param 3", "value 3"),
                        tuple("param 5", "value 5"),
                        tuple("param 6", "value 6"),
                        tuple("param 7", "******")
                );
    }

    @Test
    void shouldOrderFixturesByStartDate() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/fixtures-sort-result.json", generateTestResultName(),
                "allure2/fixtures-sort.json", generateTestResultContainerName(),
                "allure2/fixtures-sort2.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .flatExtracting(TestResult::getBeforeStages)
                .extracting(StageResult::getName)
                .containsExactly(
                        "first",
                        "second",
                        "third",
                        "fourth",
                        "last"
                );

        assertThat(testResults)
                .flatExtracting(TestResult::getAfterStages)
                .extracting(StageResult::getName)
                .containsExactly(
                        "first",
                        "second",
                        "third",
                        "fourth",
                        "last"
                );
    }

    @Test
    void shouldSetFlakyFromResults() throws IOException {
        final LaunchResults results = process(
                "allure2/flaky.json", generateTestResultName(),
                "allure2/flaky-false.json", generateTestResultName(),
                "allure2/flaky-not-set.json", generateTestResultName()
        );

        assertThat(results.getResults())
                .extracting(TestResult::getName, TestResult::isFlaky)
                .containsExactlyInAnyOrder(
                        tuple("flaky test", true),
                        tuple("not flaky test", false),
                        tuple("default not flaky test", false)
                );
    }

    private LaunchResults process(String... strings) throws IOException {
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(directory, first, second);
        }
        Allure2Plugin reader = new Allure2Plugin();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
        reader.readResults(configuration, resultsVisitor, directory);
        return resultsVisitor.getLaunchResults();
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(Objects.requireNonNull(is), dir.resolve(fileName));
        }
    }

    private static String generateTestResultName() {
        return UUID.randomUUID() + "-result.json";
    }

    private static String generateTestResultContainerName() {
        return UUID.randomUUID() + "-container.json";
    }
}
