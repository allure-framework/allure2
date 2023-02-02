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
package io.qameta.allure.allure1;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.Issue;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.UNKNOWN;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class Allure1PluginTest {

    private Path directory;

    @BeforeEach
    void setUp(@TempDir final Path directory) {
        this.directory = directory;
    }

    @Test
    void shouldProcessEmptyOrNullStatus() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/empty-status-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(4)
                .extracting("name", "status")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("testOne", UNKNOWN),
                        Tuple.tuple("testTwo", PASSED),
                        Tuple.tuple("testThree", FAILED),
                        Tuple.tuple("testFour", UNKNOWN)
                );
    }

    @Test
    void shouldReadTestSuiteXml() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(4);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldExcludeDuplicatedParams() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/duplicated-params.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(1)
                .flatExtracting(TestResult::getParameters)
                .hasSize(4)
                .extracting(Parameter::getName, Parameter::getValue)
                .containsExactlyInAnyOrder(
                        tuple("name", "value"),
                        tuple("name2", "value"),
                        tuple("name", "value2"),
                        tuple("name2", "value2")
                );
    }

    @Test
    void shouldReadTestSuiteJson() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        ).getResults();
        assertThat(testResults)
                .hasSize(1);
    }

    @Test
    void shouldReadAttachments() throws Exception {
        final LaunchResults launchResults = process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName(),
                "allure1/sample-attachment.txt", "sample-attachment.txt"
        );
        final Map<Path, Attachment> attachmentMap = launchResults.getAttachments();
        final Set<TestResult> results = launchResults.getResults();

        assertThat(attachmentMap)
                .hasSize(1);

        final Attachment storedAttachment = attachmentMap.values().iterator().next();

        List<Attachment> attachments = results.stream()
                .flatMap(this::extractAttachments)
                .collect(Collectors.toList());

        assertThat(attachments)
                .hasSize(1)
                .extracting(Attachment::getSource)
                .containsExactly(storedAttachment.getSource());
    }

    private Stream<Attachment> extractAttachments(TestResult testCaseResult) {
        Stream<StageResult> before = testCaseResult.getBeforeStages().stream();
        Stream<StageResult> test = Stream.of(testCaseResult.getTestStage());
        Stream<StageResult> after = testCaseResult.getAfterStages().stream();
        return Stream.concat(before, Stream.concat(test, after))
                .flatMap(this::extractAttachments);
    }

    private Stream<Attachment> extractAttachments(StageResult stageResult) {
        Stream<Attachment> fromSteps = stageResult.getSteps().stream().flatMap(this::extractAttachments);
        Stream<Attachment> fromAttachments = stageResult.getAttachments().stream();
        return Stream.concat(fromSteps, fromAttachments);
    }

    private Stream<Attachment> extractAttachments(Step step) {
        Stream<Attachment> fromSteps = step.getSteps().stream().flatMap(this::extractAttachments);
        Stream<Attachment> fromAttachments = step.getAttachments().stream();
        return Stream.concat(fromSteps, fromAttachments);
    }

    @Test
    void shouldNotFailIfNoResultsDirectory() throws Exception {
        Set<TestResult> testResults = process().getResults();
        assertThat(testResults)
                .isEmpty();
    }

    @Test
    void shouldGetSuiteTitleIfExists() throws Exception {
        Set<TestResult> testCases = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        ).getResults();
        assertThat(testCases)
                .hasSize(1)
                .extracting(testResult -> testResult.findOneLabel(LabelName.SUITE))
                .extracting(Optional::get)
                .containsExactly("Passing test");
    }

    @Test
    void shouldNotFailIfSuiteTitleNotExists() throws Exception {
        Set<TestResult> testCases = process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(testCases)
                .hasSize(1)
                .extracting(testResult -> testResult.findOneLabel(LabelName.SUITE))
                .extracting(Optional::get)
                .containsExactly("my.company.AlwaysPassingTest");
    }

    @Test
    void shouldCopyLabelsFromSuite() throws Exception {
        Set<TestResult> testCases = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        ).getResults();
        assertThat(testCases)
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .filteredOn(label -> LabelName.STORY.value().equals(label.getName()))
                .hasSize(2)
                .extracting(Label::getValue)
                .containsExactlyInAnyOrder("SuccessStory", "OtherStory");
    }

    @Test
    void shouldSetFlakyFromLabel() throws Exception {
        Set<TestResult> testCases = process(
                "allure1/suite-with-attachments.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testCases)
                .hasSize(1)
                .extracting(TestResult::isFlaky)
                .containsExactly(true);
    }

    @Test
    void shouldUseTestClassLabelForPackage() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/packages-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(1)
                .extracting(result -> result.findOneLabel(LabelName.PACKAGE))
                .extracting(Optional::get)
                .containsExactly("my.company.package.subpackage.MyClass");
    }

    @Test
    void shouldUseTestClassLabelForFullName() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/packages-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .extracting(TestResult::getFullName)
                .containsExactly("my.company.package.subpackage.MyClass.testThree");
    }

    @Test
    void shouldAddTestResultFormatLabel() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(testResults)
                .extracting(result -> result.findOneLabel(LabelName.RESULT_FORMAT))
                .extracting(Optional::get)
                .containsOnly(Allure1Plugin.ALLURE1_RESULTS_FORMAT);
    }

    @Test
    void shouldGenerateDifferentHistoryIdForParameterizedTests() throws Exception {
        final String historyId1 = "56f15d234f8ad63b493afb25f7c26556";
        final String historyId2 = "e374f6eb3cf497543291506c8c20353";
        Set<TestResult> testResults = process(
                "allure1/suite-with-parameters.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(testResults)
                .extracting(TestResult::getHistoryId)
                .as("History ids for parameterized tests must be different")
                .containsExactlyInAnyOrder(historyId1, historyId2);
    }

    @Test
    void shouldReadPropertiesFile() throws Exception {
        final String testName = "testFour";
        final String link1 = "http://example.org/JIRA-1";
        final String link2 = "http://example.org/JIRA-2";
        final String link3 = "http://example.org/TMS-1";
        Set<TestResult> testResults = process(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName(),
                "allure1/allure.properties", "allure.properties"
        ).getResults();

        assertThat(testResults)
                .filteredOn(testResult -> testResult.getName().equals(testName))
                .flatExtracting(TestResult::getLinks)
                .extracting(Link::getUrl)
                .as("Test links should contain patterns from allure.properties file")
                .containsExactlyInAnyOrder(link1, link2, link3);
    }

    @Test
    void shouldProcessNullParameters() throws Exception {
        final Set<TestResult> results = process(
                "allure1/empty-parameter-value.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(results)
                .hasSize(1)
                .flatExtracting(TestResult::getParameters)
                .hasSize(4)
                .extracting(Parameter::getName, Parameter::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("parameterArgument", null),
                        Tuple.tuple("parameter", "default"),
                        Tuple.tuple("invalid", null),
                        Tuple.tuple(null, null)
                );
    }

    @Test
    void shouldBeAbleToSpecifyHistoryIdViaLabel() throws Exception {
        final Set<TestResult> results = process(
                "allure1/history-id-label.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(results)
                .filteredOn("name", "test1")
                .extracting(TestResult::getHistoryId)
                .containsExactly("something");

        assertThat(results)
                .filteredOn("name", "test2")
                .extracting(TestResult::getHistoryId)
                .containsNull();
    }

    @Issue("629")
    @Test
    void shouldProcessEmptyLists() throws Exception {
        final Set<TestResult> results = process(
                "allure1/empty-lists.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(results)
                .hasSize(1);
    }

    private LaunchResults process(String... strings) throws IOException {
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(directory, first, second);
        }
        Allure1Plugin reader = new Allure1Plugin();
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
        reader.readResults(configuration, resultsVisitor, directory);
        return resultsVisitor.getLaunchResults();
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}
