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
package io.qameta.allure.allure1;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.Description;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.UNKNOWN;
import static io.qameta.allure.testdata.TestData.attachFileContent;
import static io.qameta.allure.testdata.TestData.attachLaunchResults;
import static io.qameta.allure.testdata.TestData.toHex;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class Allure1PluginTest {

    private static final String TEXT_PLAIN = "text/plain";

    private Path directory;

    @BeforeEach
    void setUp(@TempDir final Path directory) {
        this.directory = directory;
    }

    /**
     * Verifies processing empty or null status for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies reading test suite XML for Allure 1 parsing.
     */
    @Description
    @Test
    void shouldReadTestSuiteXml() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/sample-testsuite.xml", generateTestSuiteXmlName()
        ).getResults();
        assertThat(testResults)
                .hasSize(4);
    }

    /**
     * Verifies sanitizing description HTML for Allure 1 parsing.
     */
    @Description
    @Test
    void shouldSanitizeDescriptionHtml() throws Exception {
        final Set<TestResult> testResults = process(
                "allure1/description-html-xss.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(testResults).hasSize(1);
        final String descriptionHtml = testResults.iterator().next().getDescriptionHtml();
        assertThat(descriptionHtml)
                .contains("<p>safe</p>")
                .doesNotContain("<script")
                .doesNotContain("alert(");
    }

    /**
     * Verifies excluding duplicated parameters for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies reading test suite JSON for Allure 1 parsing.
     */
    @Description
    @Test
    void shouldReadTestSuiteJson() throws Exception {
        Set<TestResult> testResults = process(
                "allure1/sample-testsuite.json", generateTestSuiteJsonName()
        ).getResults();
        assertThat(testResults)
                .hasSize(1);
    }

    /**
     * Verifies reading attachments for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies that a missing results directory does not fail parsing.
     */
    @Description
    @Test
    void shouldNotFailIfNoResultsDirectory() throws Exception {
        Set<TestResult> testResults = process().getResults();
        assertThat(testResults)
                .isEmpty();
    }

    /**
     * Verifies resolving suite title if exists for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies suite label fallback when an Allure 1 suite title is missing.
     */
    @Description
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

    /**
     * Verifies copying labels from suite for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies deriving the flaky flag from an Allure 1 label.
     */
    @Description
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

    /**
     * Verifies deriving the package from the Allure 1 test class label.
     */
    @Description
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

    /**
     * Verifies deriving the full name from the Allure 1 test class label.
     */
    @Description
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

    /**
     * Verifies adding the test result format label for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies generating different history id for parameterized tests for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies reading properties file for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies processing null parameters for Allure 1 parsing.
     */
    @Description
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

    /**
     * Verifies that an Allure 1 history-id label overrides generated history ids.
     */
    @Description
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

    /**
     * Verifies processing empty lists for Allure 1 parsing.
     */
    @Description
    @Issue("629")
    @Test
    void shouldProcessEmptyLists() throws Exception {
        final Set<TestResult> results = process(
                "allure1/empty-lists.xml", generateTestSuiteXmlName()
        ).getResults();

        assertThat(results)
                .hasSize(1);
    }

    /**
     * Verifies preserving content type from attachment for Allure 1 parsing.
     */
    @Description
    @Test
    void shouldPreserveContentTypeFromAttachment() throws IOException {
        final LaunchResults results = process(
                "allure1/text-attachment-no-ext.xml", generateTestSuiteXmlName(),
                "allure1/sample-attachment.txt", "test-sample-attachment"
        );

        assertThat(results.getResults())
                .hasSize(1);

        final TestResult tr = results.getResults().iterator().next();
        final List<Attachment> attachments = tr.getTestStage().getAttachments();
        assertThat(attachments)
                .extracting(Attachment::getName, Attachment::getType, Attachment::getSize)
                .containsExactlyInAnyOrder(
                        tuple("String attachment in test", "text/plain", 25L)
                );

        assertThat(attachments.get(0).getSource()).endsWith(".txt");
    }

    /**
     * Verifies resolving attachments with relative results path for Allure 1 parsing.
     */
    @Description
    @Test
    void shouldResolveAttachmentsWithRelativeResultsPath() throws IOException {
        final Path allureResults = directory.resolve("allure-results");
        Files.createDirectories(allureResults);

        copyFile(allureResults, "allure1/text-attachment-link.xml", generateTestSuiteXmlName());
        copyFile(allureResults, "allure1/sample-attachment.txt", "link.txt");
        final Allure1Plugin reader = new Allure1Plugin();
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final Path relative = allureResults.resolve("..").resolve("allure-results");
        final LaunchResults results = readResults(reader, configuration, relative);

        assertThat(results.getResults())
                .hasSize(1);

        final TestResult tr = results.getResults().iterator().next();
        final List<Attachment> attachments = tr.getTestStage().getAttachments();
        assertThat(attachments)
                .extracting(Attachment::getName, Attachment::getType, Attachment::getSize)
                .containsExactlyInAnyOrder(
                        tuple("String attachment in test", "text/plain", 25L)
                );

        assertThat(attachments.get(0).getSource()).endsWith(".txt");
    }

    /**
     * Verifies reading environment properties UTF-8 for Allure 1 parsing.
     */
    @Description
    @SuppressWarnings("unchecked")
    @Test
    void shouldReadEnvironmentPropertiesUtf8() throws Exception {
        writeBytes("test_executor=测试人员 A\n".getBytes(StandardCharsets.UTF_8));

        final LaunchResults launchResults = process();
        final Map<String, String> env = launchResults.getExtra(
                Allure1Plugin.ENVIRONMENT_BLOCK_NAME,
                (Supplier<Map<String, String>>) LinkedHashMap::new
        );

        assertThat(env).containsEntry("test_executor", "测试人员 A");
    }

    /**
     * Verifies reading environment properties UTF-8 with BOM for Allure 1 parsing.
     */
    @Description
    @SuppressWarnings("unchecked")
    @Test
    void shouldReadEnvironmentPropertiesUtf8WithBom() throws Exception {
        final byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        final byte[] content = "executor=测试人员 A\n".getBytes(StandardCharsets.UTF_8);
        final byte[] bytes = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, bytes, 0, bom.length);
        System.arraycopy(content, 0, bytes, bom.length, content.length);

        writeBytes(bytes);

        final LaunchResults launchResults = process();
        final Map<String, String> env = launchResults.getExtra(
                Allure1Plugin.ENVIRONMENT_BLOCK_NAME,
                (Supplier<Map<String, String>>) LinkedHashMap::new
        );

                assertThat(env).containsEntry("executor", "测试人员 A");
                assertThat(env).doesNotContainKey("\uFEFFexecutor");
    }

    /**
     * Verifies falling back to ISO-8859-1 when UTF-8 environment decoding fails.
     */
    @Description
    @SuppressWarnings("unchecked")
    @Test
    void shouldFallbackToIso88591WhenUtf8DecodingFails() throws Exception {
        writeBytes(
                "name=café\n".getBytes(StandardCharsets.ISO_8859_1));

        final LaunchResults launchResults = process();
        final Map<String, String> env = launchResults.getExtra(
                Allure1Plugin.ENVIRONMENT_BLOCK_NAME,
                (Supplier<Map<String, String>>) LinkedHashMap::new
        );

        assertThat(env).containsEntry("name", "café");
    }

    /**
     * Verifies rejecting attachment sources with invalid characters.
     */
    @Description
    @Test
    void shouldNotAllowInvalidCharactersInAttachmentSource() throws IOException {
        final LaunchResults results = process(
                "allure1/text-attachment-bad-source.xml", generateTestSuiteXmlName(),
                "allure1/secret-file.txt", "secret-file.txt"
        );

        assertThat(results.getAttachments())
                .isEmpty();

    }

    /**
     * Verifies rejecting attachment source path traversal attempts.
     */
    @Description
    @Test
    void shouldNotAllowAttachmentSourcePathTraversal() throws IOException {
        final Path allureResultsDir = directory.resolve("allure-results");
        Files.createDirectory(allureResultsDir);

        copyFile(allureResultsDir, "allure1/text-attachment-path-traversal.xml", generateTestSuiteXmlName());
        copyFile(directory, "allure1/secret-file.txt", "secret-file.txt");

        final Allure1Plugin reader = new Allure1Plugin();
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final LaunchResults results = readResults(reader, configuration, allureResultsDir);

        assertThat(results.getAttachments())
                .isEmpty();

    }

    /**
     * Verifies rejecting attachment sources that resolve through symbolic links.
     */
    @Description
    @Test
    void shouldNotAllowAttachmentSourceSymbolicLink() throws IOException {
        final Path allureResultsDir = directory.resolve("allure-results");
        Files.createDirectory(allureResultsDir);

        copyFile(allureResultsDir, "allure1/text-attachment-link.xml", generateTestSuiteXmlName());
        copyFile(allureResultsDir, "allure1/secret-file.txt", "secret-file.txt");

        Files.createSymbolicLink(allureResultsDir.resolve("link.txt"), allureResultsDir.resolve("secret-file.txt"));

        final Allure1Plugin reader = new Allure1Plugin();
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final LaunchResults results = readResults(reader, configuration, allureResultsDir);

        assertThat(results.getAttachments())
                .isEmpty();

    }

    private LaunchResults process(String... strings) throws IOException {
        return Allure.step(
                "Read Allure 1 launch from " + strings.length / 2 + " fixture file(s)",
                () -> {
                    Iterator<String> iterator = Arrays.asList(strings).iterator();
                    while (iterator.hasNext()) {
                        String first = iterator.next();
                        String second = iterator.next();
                        copyFile(directory, first, second);
                    }
                    final Allure1Plugin reader = new Allure1Plugin();
                    final Configuration configuration = ConfigurationBuilder.bundled().build();
                    return readResults(reader, configuration, directory);
                }
        );
    }

    private void writeBytes(final byte[] bytes) throws IOException {
        Allure.step("Write environment.properties with " + bytes.length + " byte(s)", () -> {
            final Path output = directory.resolve("environment.properties");
            Files.write(output, bytes);
            Allure.addAttachment("environment.properties", TEXT_PLAIN, describeEnvironmentProperties(bytes));
        });
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        Allure.step("Copy fixture " + resourceName + " as " + fileName, () -> {
            final Path output = dir.resolve(fileName);
            final byte[] content;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                content = Objects.requireNonNull(is, "resource " + resourceName + " not found").readAllBytes();
                Files.write(output, content);
            }
            attachFileContent(fileName, content);
        });
    }

    private LaunchResults readResults(
            final Allure1Plugin reader,
            final Configuration configuration,
            final Path resultsDirectory
    ) {
        return Allure.step("Parse Allure 1 results from " + resultsDirectory, () -> {
            final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
            reader.readResults(configuration, resultsVisitor, resultsDirectory);
            final LaunchResults results = resultsVisitor.getLaunchResults();
            attachLaunchResults("Attach parsed Allure 1 launch artifacts", results);
            return results;
        });
    }

    private String describeEnvironmentProperties(final byte[] bytes) {
        return String.format(
                "utf8=%s%niso88591=%s%nhex=%s%n",
                new String(bytes, StandardCharsets.UTF_8),
                new String(bytes, StandardCharsets.ISO_8859_1),
                toHex(bytes)
        );
    }
}
