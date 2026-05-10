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
package io.qameta.allure.allure2;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultResultsVisitor;
import io.qameta.allure.Description;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.qameta.allure.entity.Status.UNKNOWN;
import static io.qameta.allure.testdata.TestData.attachFileContent;
import static io.qameta.allure.testdata.TestData.attachLaunchResults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class Allure2PluginTest {

    private Path directory;

    @BeforeEach
    void setUp(@TempDir final Path directory) {
        this.directory = directory;
    }

    /**
     * Verifies reading befores from groups for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies reading afters from groups for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies excluding duplicated parameters for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies picking up attachments for test case for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies picking up attachments for afters for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies that group attachments are not overwritten for Allure 2 parsing.
     */
    @Description
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

        testResults.forEach(
                testResult -> assertThat(testResult.getAfterStages())
                        .hasSize(1)
                        .flatExtracting(StageResult::getAttachments)
                        .hasSize(1)
                        .extracting(Attachment::getName)
                        .containsExactly("String attachment in after")
        );

    }

    /**
     * Verifies processing empty status for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies processing null status for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies processing invalid status for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies that {@code titlePath} from the on-disk Allure 2 schema is
     * preserved on the entity through the extra-block channel, keeping the
     * data reachable for downstream widgets and matching Allure 3 reader
     * behaviour.
     */
    @Description
    @Test
    void shouldPreserveTitlePathFromOnDiskSchema() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/title-path.json", generateTestResultName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1)
                .first()
                .satisfies(
                        result -> assertThat(result.<List<String>>getExtraBlock("titlePath"))
                                .as("titlePath from on-disk schema should be preserved on the entity")
                                .containsExactly("Suite", "Subsuite", "Group")
                );
    }

    /**
     * Verifies processing null stage time for Allure 2 parsing.
     */
    @Description
    @Test
    void shouldProcessNullStageTime() throws Exception {
        Set<TestResult> testResults = process(
                "allure2/other-testcase.json", generateTestResultName(),
                "allure2/null-before-group.json", generateTestResultContainerName()
        ).getResults();

        assertThat(testResults)
                .hasSize(1);
    }

    /**
     * Verifies adding the test result format label for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies processing parameters for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies processing step parameters for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies ordering fixtures by start date for Allure 2 parsing.
     */
    @Description
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

    /**
     * Verifies deriving the flaky flag from Allure 2 result data.
     */
    @Description
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

    /**
     * Verifies sanitizing description HTML for Allure 2 parsing.
     */
    @Description
    @Test
    void shouldSanitizeDescriptionHtml() throws Exception {
        final LaunchResults results = process(
                "allure2/description-html-xss.json", generateTestResultName()
        );

        assertThat(results.getResults())
                .hasSize(1);

        final TestResult testResult = results.getResults().iterator().next();
        final String descriptionHtml = testResult.getDescriptionHtml();
        assertThat(descriptionHtml)
                .contains("<p>safe</p>")
                .doesNotContain("<img")
                .doesNotContain("onerror")
                .doesNotContain("javascript:");
    }

    /**
     * Verifies script tags are stripped from Allure 2 HTML descriptions.
     */
    @Description
    @Test
    void shouldStripScriptTagsFromDescriptionHtml() throws Exception {
        final LaunchResults results = process(
                "allure2/description-html-script-xss.json", generateTestResultName()
        );

        assertThat(results.getResults())
                .hasSize(1);

        final TestResult testResult = results.getResults().iterator().next();
        final String descriptionHtml = testResult.getDescriptionHtml();
        assertThat(descriptionHtml)
                .contains("<p>safe</p>")
                .doesNotContain("<script")
                .doesNotContain("alert(");
    }

    /**
     * Verifies preserving content type from attachment for Allure 2 parsing.
     */
    @Description
    @Test
    void shouldPreserveContentTypeFromAttachment() throws IOException {
        final LaunchResults results = process(
                "allure2/text-attachment-no-ext.json", generateTestResultName(),
                "allure2/test-sample-attachment.txt", "test-sample-attachment"
        );

        assertThat(results.getResults())
                .hasSize(1);

        final TestResult tr = results.getResults().iterator().next();
        final List<Attachment> attachments = tr.getTestStage().getAttachments();
        assertThat(attachments)
                .extracting(Attachment::getName, Attachment::getType, Attachment::getSize)
                .containsExactlyInAnyOrder(
                        tuple("String attachment in test", "text/plain", 24L)
                );

        assertThat(attachments.get(0).getSource()).endsWith(".txt");
    }

    /**
     * Verifies rejecting attachment sources with invalid characters.
     */
    @Description
    @Test
    void shouldNotAllowInvalidCharactersInAttachmentSource() throws IOException {
        final LaunchResults results = process(
                "allure2/text-attachment-bad-source.json", generateTestResultName(),
                "allure2/secret-file.txt", "secret-file.txt"
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

        copyFile(allureResultsDir, "allure2/text-attachment-path-traversal.json", generateTestResultName());
        copyFile(directory, "allure2/secret-file.txt", "secret-file.txt");

        final Allure2Plugin reader = new Allure2Plugin();
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

        copyFile(allureResultsDir, "allure2/text-attachment-link.json", generateTestResultName());
        copyFile(allureResultsDir, "allure2/secret-file.txt", "secret-file.txt");

        Files.createSymbolicLink(allureResultsDir.resolve("link.txt"), allureResultsDir.resolve("secret-file.txt"));

        final Allure2Plugin reader = new Allure2Plugin();
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final LaunchResults results = readResults(reader, configuration, allureResultsDir);

        assertThat(results.getAttachments())
                .isEmpty();

    }

    /**
     * Verifies resolving attachments with relative results path for Allure 2 parsing.
     */
    @Description
    @Test
    void shouldResolveAttachmentsWithRelativeResultsPath() throws IOException {
        final Path allureResults = directory.resolve("allure-results");
        Files.createDirectories(allureResults);
        copyFile(allureResults, "allure2/text-attachment-link.json", generateTestResultName());
        copyFile(allureResults, "allure2/test-sample-attachment.txt", "link.txt");

        final Allure2Plugin reader = new Allure2Plugin();
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
                        tuple("String attachment in test", "text/plain", 24L)
                );

        assertThat(attachments.get(0).getSource()).endsWith(".txt");
    }

    private LaunchResults process(String... strings) throws IOException {
        return Allure.step(
                "Read Allure 2 launch from " + strings.length / 2 + " fixture file(s)",
                () -> {
                    Iterator<String> iterator = Arrays.asList(strings).iterator();
                    while (iterator.hasNext()) {
                        String first = iterator.next();
                        String second = iterator.next();
                        copyFile(directory, first, second);
                    }
                    final Allure2Plugin reader = new Allure2Plugin();
                    final Configuration configuration = ConfigurationBuilder.bundled().build();
                    return readResults(reader, configuration, directory);
                }
        );
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        Allure.step("Copy fixture " + resourceName + " as " + fileName, () -> {
            final Path output = dir.resolve(fileName);
            final byte[] content;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                content = Objects.requireNonNull(is).readAllBytes();
                Files.write(output, content);
            }
            attachFileContent(fileName, content);
        });
    }

    private LaunchResults readResults(
                                      final Allure2Plugin reader,
                                      final Configuration configuration,
                                      final Path resultsDirectory) {
        return Allure.step("Parse Allure 2 results from " + resultsDirectory, () -> {
            final DefaultResultsVisitor resultsVisitor = new DefaultResultsVisitor(configuration);
            reader.readResults(configuration, resultsVisitor, resultsDirectory);
            final LaunchResults results = resultsVisitor.getLaunchResults();
            attachLaunchResults("Attach parsed Allure 2 launch artifacts", results);
            return results;
        });
    }

    private static String generateTestResultName() {
        return UUID.randomUUID() + "-result.json";
    }

    private static String generateTestResultContainerName() {
        return UUID.randomUUID() + "-container.json";
    }
}
