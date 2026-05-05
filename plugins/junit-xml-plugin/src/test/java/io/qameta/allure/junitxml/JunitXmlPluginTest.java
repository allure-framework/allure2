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
package io.qameta.allure.junitxml;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class JunitXmlPluginTest {

    private Configuration configuration;
    private ResultsVisitor visitor;
    private Path resultsDirectory;

    @BeforeEach
    void setUp(@TempDir final Path resultsDirectory) {
        configuration = mock(Configuration.class);
        when(configuration.requireContext(RandomUidContext.class)).thenReturn(new RandomUidContext());
        visitor = mock(ResultsVisitor.class);
        this.resultsDirectory = resultsDirectory;
    }

    /**
     * Verifies a JUnit XML report is converted into Allure test results.
     * The test checks the parsed result count and status distribution.
     */
    @Description
    @Test
    void shouldReadJunitResults() throws Exception {
        process(
                "junitdata/TEST-org.allurefw.report.junit.JunitTestResultsTest.xml",
                "TEST-org.allurefw.report.junit.JunitTestResultsTest.xml"
        );

        final List<TestResult> results = captureTestResults(5);

        assertThat(results)
                .hasSize(5);

        final List<TestResult> failed = filterByStatus(results, Status.FAILED);
        final List<TestResult> skipped = filterByStatus(results, Status.SKIPPED);
        final List<TestResult> passed = filterByStatus(results, Status.PASSED);

        assertThat(failed)
                .describedAs("Should parse failed status")
                .hasSize(1);

        assertThat(skipped)
                .describedAs("Should parse skipped status")
                .hasSize(1);

        assertThat(passed)
                .describedAs("Should parse passed status")
                .hasSize(3);
    }

    /**
     * Verifies a JUnit text log is linked as an Allure stage attachment.
     * The test checks the visited attachment content and the attachment name and UID in the parsed result.
     */
    @Description
    @Test
    void shouldAddLogAsAttachment() throws Exception {
        final Attachment hey = new Attachment().setUid("some-uid");
        when(visitor.visitAttachmentFile(any())).thenReturn(hey);
        process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml",
                "junitdata/test.SampleTest.txt", "test.SampleTest.txt"
        );

        final List<Path> attachments = captureAttachmentFiles(1);

        assertThat(attachments.get(0))
                .isRegularFile()
                .hasContent("some-test-log");

        final List<TestResult> results = captureTestResults(1);

        final StageResult testStage = results.get(0).getTestStage();
        assertThat(testStage)
                .describedAs("Should create a test stage")
                .isNotNull();

        assertThat(testStage.getAttachments())
                .describedAs("Should add an attachment")
                .hasSize(1)
                .describedAs("Attachment should has right uid and name")
                .extracting(Attachment::getName, Attachment::getUid)
                .containsExactly(Tuple.tuple("System out", "some-uid"));
    }

    /**
     * Verifies JUnit log attachments resolve when the results path is relative.
     * The test checks the attachment visitor receives the normalized log file content.
     */
    @Description
    @Test
    void shouldResolveAttachmentsWithRelativeResultsPath() throws Exception {
        final Attachment hey = new Attachment().setUid("some-uid");
        when(visitor.visitAttachmentFile(any())).thenReturn(hey);
        final Path junitResults = resultsDirectory.resolve("junit-results");
        Files.createDirectories(junitResults);

        copyFile(junitResults,     "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml");
        copyFile(junitResults, "junitdata/test.SampleTest.txt", "test.SampleTest.txt");
        final Path relative = junitResults.resolve("..").resolve("junit-results");
        readResults(relative);

        final List<Path> attachments = captureAttachmentFiles(1);

        assertThat(attachments.get(0))
                .isRegularFile()
                .hasContent("some-test-log");

    }

    /**
     * Verifies JUnit attachment paths cannot escape the results directory.
     * The test checks a traversal fixture does not send the secret file to the visitor.
     */
    @Description
    @Test
    void shouldNotAllowPathTraversal() throws Exception {
        final Attachment hey = new Attachment().setUid("some-uid");
        when(visitor.visitAttachmentFile(any())).thenReturn(hey);

        final Path junitResults = resultsDirectory.resolve("junit-results");
        Files.createDirectories(junitResults);

        copyFile(junitResults, "junitdata/path-traversal.xml", "TEST-test.SampleTest.xml");
        copyFile(resultsDirectory, "junitdata/secret-file.txt", "secret-file.txt");

        readResults(junitResults);

        verifyNoAttachmentFiles();
    }

    /**
     * Verifies JUnit suite, package, class, and format labels are emitted.
     * The test checks the exact label set parsed from a simple fixture.
     */
    @Description
    @Test
    void shouldAddLabels() throws Exception {
        process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .extracting(Label::getName, Label::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LabelName.SUITE.value(), "test.SampleTest"),
                        Tuple.tuple(LabelName.PACKAGE.value(), "test.SampleTest"),
                        Tuple.tuple(LabelName.TEST_CLASS.value(), "test.SampleTest"),
                        Tuple.tuple(LabelName.RESULT_FORMAT.value(), JunitXmlPlugin.JUNIT_RESULTS_FORMAT)
                );
    }

    /**
     * Verifies invalid JUnit XML files are skipped safely.
     * The test checks no test result is emitted for an invalid XML fixture.
     */
    @Description
    @Test
    void shouldSkipInvalidXml() throws Exception {
        process(
                "junitdata/invalid.xml", "sample-testsuite.xml"
        );

        verifyNoTestResults();
    }

    /**
     * Verifies repeated JUnit results are modeled as retries.
     * The test checks visible and hidden retry flags, history IDs, and status details.
     */
    @Description
    @Test
    void shouldProcessTestsWithRetry() throws Exception {
        process(
                "junitdata/TEST-test.RetryTest.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(4);
        assertThat(results)
                .extracting(TestResult::getName, TestResult::getStatus, TestResult::isHidden, TestResult::getHistoryId)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("searchTest", Status.BROKEN, false, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", Status.BROKEN, true, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", Status.BROKEN, true, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", Status.FAILED, true, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest")
                );

        assertThat(results)
                .extracting(TestResult::getStatusMessage, TestResult::getStatusTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("message-root", "trace-root"),
                        Tuple.tuple("message-retried-1", "trace-retried-1"),
                        Tuple.tuple("message-retried-2", "trace-retried-2"),
                        Tuple.tuple("message-retried-3", "trace-retried-3")
                );
    }

    /**
     * Verifies JUnit failure CDATA is parsed into status details.
     * The test checks the exact message and trace values for failed and passed cases.
     */
    @Description
    @Test
    void shouldReadStatusMessage() throws Exception {
        process(
                "junitdata/TEST-test.CdataMessage.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(2);

        assertThat(results)
                .extracting(TestResult::getStatusMessage, TestResult::getStatusTrace)
                .containsExactlyInAnyOrder(
                        tuple("some-message", "some-trace"),
                        tuple(null, null)
                );
    }

    /**
     * Verifies JUnit system output is converted into Allure steps.
     * The test checks the parsed step names generated from stdout content.
     */
    @Description
    @Test
    void shouldReadSystemOutMessage() throws Exception {
        process(
                "junitdata/TEST-test.CdataMessage.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(2);

        assertThat(results)
                .filteredOn(result -> result.getTestStage().getSteps().size() == 2)
                .filteredOn(result -> result.getTestStage().getSteps().get(0).getName().equals("output"))
                .filteredOn(result -> result.getTestStage().getSteps().get(1).getName().equals("more output"))
                .hasSize(1);
    }

    /**
     * Verifies JUnit reports wrapped in a testsuites tag are parsed.
     * The test checks all child testcase names are emitted as Allure results.
     */
    @Issue("532")
    @Description
    @Test
    void shouldParseSuitesTag() throws Exception {
        process(
                "junitdata/testsuites.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(3);

        assertThat(results)
                .extracting(TestResult::getName)
                .containsExactlyInAnyOrder(
                        "should default path to an empty string",
                        "should default consolidate to true",
                        "should default useDotNotation to true"
                );
    }

    /**
     * Verifies JUnit timestamps are converted into Allure start, stop, and duration.
     * The test checks the exact time values parsed from a timestamped fixture.
     */
    @Description
    @Test
    void shouldProcessTimestampIfPresent() throws Exception {
        process(
                "junitdata/with-timestamp.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .extracting(TestResult::getTime)
                .extracting(Time::getStart, Time::getStop, Time::getDuration)
                .containsExactlyInAnyOrder(
                        tuple(1507199782999L, 1507199784050L, 1051L)
                );
    }

    /**
     * Verifies a JUnit suite name overrides the default suite label.
     * The test checks the exact suite label parsed from the fixture.
     */
    @Description
    @Test
    void shouldUseSuiteNameIfPresent() throws Exception {
        process(
                "junitdata/with-timestamp.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .flatExtracting(TestResult::getLabels)
                .filteredOn("name", "suite")
                .extracting(Label::getValue)
                .containsExactly("LocalSuiteIDOL");

    }

    /**
     * Verifies a JUnit hostname attribute becomes an Allure host label.
     * The test checks the exact host label parsed from the fixture.
     */
    @Description
    @Test
    void shouldUseHostnameIfPresent() throws Exception {
        process(
                "junitdata/with-timestamp.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .flatExtracting(TestResult::getLabels)
                .filteredOn("name", "host")
                .extracting(Label::getValue)
                .containsExactly("cbgtalosbld02");

    }

    /**
     * Verifies JUnit skipped status is parsed from elements and attributes.
     * The test checks the resulting skipped result count for the status fixture.
     */
    @Description
    @Test
    void shouldReadSkippedStatus() throws Exception {
        process(
                "junitdata/TEST-status-attribute.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(3);

        final List<TestResult> skipped = filterByStatus(results, Status.SKIPPED);

        assertThat(skipped)
                .describedAs("Should parse skipped elements and status attribute")
                .hasSize(2);

    }

    /**
     * Verifies JUnit testcase properties are mapped into Allure parameters.
     * The test checks the exact parsed parameter names and values.
     */
    @Description
    @Test
    void shouldUsePropertiesIfPresent() throws Exception {
        process(
                "junitdata/TEST-test.PropertiesTest.xml", "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .flatExtracting(TestResult::getParameters)
                .extracting(Parameter::getName, Parameter::getValue)
                .containsExactlyInAnyOrder(
                        tuple("foo", "bar"),
                        tuple("baz", "some value")
                );
    }

    /**
     * Verifies JUnit files with Zulu timestamps preserve timing fields.
     * The test checks parsed start, stop, and duration for both test cases.
     */
    @Description
    @Test
    void shouldProcessFilesWithZuluTimestamp() throws Exception {
        process(
                "junitdata/zulu-timestamp.xml",
                "TEST-test.SampleTest.xml"
        );

        final List<TestResult> results = captureTestResults(2);

        assertThat(results)
                .extracting(TestResult::getTime)
                .extracting(Time::getStart, Time::getStop, Time::getDuration)
                .containsExactlyInAnyOrder(
                        tuple(1525592511000L, 1525592527211L, 16211L),
                        tuple(1525592511000L, 1525592519477L, 8477L)
                );
    }

    /**
     * Verifies external XML entities are not resolved while parsing JUnit XML.
     * The test checks parsed status traces do not expose content from a local secret file.
     */
    @Description
    @Test
    void cveEntityReadTest(@TempDir final Path tmp) throws IOException {
        final Path secretFile = tmp.resolve("secretfile.ini");
        Files.writeString(secretFile,
                "[owner]\n"
                + "name = John Doe\n"
                + "organization = Example Org.\n",
                StandardCharsets.UTF_8
        );

        final String maliciousXml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE foo [\n"
                + "  <!ENTITY xxe SYSTEM \"" + secretFile.toUri() + "\">\n"
                + "]>\n"
                + "<testsuite tests=\"5\" failures=\"1\" name=\"org.allurefw.report.junit.JunitXmlPluginTest\" time=\"0.354\" errors=\"0\" skipped=\"1\">\n"
                + "  <testcase classname=\"org.allurefw.report.junit.JunitXmlPluginTest\" name=\"shouldReadFailures\" time=\"0.012\">\n"
                + "    <failure message=\"message\">&xxe;\n"
                + "    </failure>\n"
                + "  </testcase>\n"
                + "</testsuite>";
        writeTextFile(resultsDirectory.resolve("bad-test.xml"), "bad-test.xml", maliciousXml);

        readResults(resultsDirectory);

        final List<TestResult> results = captureTestResults(1);

        final TestResult testResult = results.get(0);
        assertThat(testResult.getStatusTrace()).doesNotContain("John Doe").doesNotContain("Example Org");
    }

    private void process(final String... strings) throws IOException {
        Allure.step("Parse JUnit XML results", () -> {
            final Iterator<String> iterator = Arrays.asList(strings).iterator();
            while (iterator.hasNext()) {
                final String first = iterator.next();
                final String second = iterator.next();
                copyFile(resultsDirectory, first, second);
            }
            readResults(resultsDirectory);
        });
    }

    private void readResults(final Path directory) {
        Allure.step("Read JUnit XML results", () -> {
            final JunitXmlPlugin reader = new JunitXmlPlugin(ZoneOffset.UTC);
            reader.readResults(configuration, visitor, directory);
        });
    }

    private void copyFile(final Path dir, final String resourceName, final String fileName) throws IOException {
        Allure.step("Copy fixture resource " + resourceName + " as " + fileName, () -> {
            final byte[] content;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                content = Objects.requireNonNull(is).readAllBytes();
            }
            final Path destination = dir.resolve(fileName);
            Files.createDirectories(destination.getParent());
            Files.write(destination, content);
            Allure.addAttachment(
                    fileName,
                    contentType(fileName),
                    new String(content, StandardCharsets.UTF_8),
                    extension(fileName)
            );
        });
    }

    private void writeTextFile(final Path path, final String fileName, final String content) throws IOException {
        Allure.step("Write text fixture " + fileName, () -> {
            Files.writeString(path, content, StandardCharsets.UTF_8);
            Allure.addAttachment(fileName, contentType(fileName), content, extension(fileName));
        });
    }

    private List<TestResult> captureTestResults(final int expectedCount) {
        return Allure.step("Capture parsed JUnit test results", () -> {
            final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
            verify(visitor, times(expectedCount)).visitTestResult(captor.capture());
            final List<TestResult> results = captor.getAllValues();
            Allure.addAttachment("parsed-test-results.txt", "text/plain", describeTestResults(results));
            return results;
        });
    }

    private List<Path> captureAttachmentFiles(final int expectedCount) throws IOException {
        return Allure.step("Capture visited attachment files", () -> {
            final ArgumentCaptor<Path> captor = ArgumentCaptor.captor();
            verify(visitor, times(expectedCount)).visitAttachmentFile(captor.capture());
            final List<Path> attachments = captor.getAllValues();
            Allure.addAttachment("visited-attachments.txt", "text/plain", describeAttachments(attachments));
            return attachments;
        });
    }

    private void verifyNoAttachmentFiles() {
        Allure.step("Verify no attachment files were visited", () -> {
            verify(visitor, times(0)).visitAttachmentFile(any());
            Allure.addAttachment("visited-attachments.txt", "text/plain", "attachments=0");
        });
    }

    private void verifyNoTestResults() {
        Allure.step("Verify no JUnit test results were emitted", () -> {
            verify(visitor, times(0)).visitTestResult(any());
            Allure.addAttachment("parsed-test-results.txt", "text/plain", "results=0");
        });
    }

    private List<TestResult> filterByStatus(final List<TestResult> testCases, final Status status) {
        return Allure.step("Filter parsed results by " + status, () -> testCases.stream()
                .filter(item -> status.equals(item.getStatus()))
                .collect(Collectors.toList()));
    }

    private String describeTestResults(final List<TestResult> results) {
        final StringBuilder builder = new StringBuilder();
        builder.append("results=").append(results.size()).append(System.lineSeparator());
        results.forEach(result -> builder
                .append(System.lineSeparator())
                .append("name=").append(result.getName()).append(System.lineSeparator())
                .append("status=").append(result.getStatus()).append(System.lineSeparator())
                .append("hidden=").append(result.isHidden()).append(System.lineSeparator())
                .append("historyId=").append(result.getHistoryId()).append(System.lineSeparator())
                .append("statusMessage=").append(result.getStatusMessage()).append(System.lineSeparator())
                .append("statusTrace=").append(result.getStatusTrace()).append(System.lineSeparator())
                .append("time=").append(describeTime(result.getTime())).append(System.lineSeparator())
                .append("labels=").append(describeLabels(result)).append(System.lineSeparator())
                .append("parameters=").append(describeParameters(result)).append(System.lineSeparator())
                .append("steps=").append(describeSteps(result)).append(System.lineSeparator())
                .append("attachments=").append(describeStageAttachments(result)).append(System.lineSeparator())
        );
        return builder.toString();
    }

    private String describeAttachments(final List<Path> attachments) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append("attachments=").append(attachments.size()).append(System.lineSeparator());
        for (Path attachment : attachments) {
            builder
                    .append(System.lineSeparator())
                    .append("file=").append(attachment.getFileName()).append(System.lineSeparator())
                    .append(Files.readString(attachment));
        }
        return builder.toString();
    }

    private String describeTime(final Time time) {
        if (time == null) {
            return null;
        }
        return String.format("start=%s, stop=%s, duration=%s", time.getStart(), time.getStop(), time.getDuration());
    }

    private String describeLabels(final TestResult result) {
        return result.getLabels().stream()
                .map(label -> label.getName() + "=" + label.getValue())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private String describeParameters(final TestResult result) {
        return result.getParameters().stream()
                .map(parameter -> parameter.getName() + "=" + parameter.getValue())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private String describeSteps(final TestResult result) {
        if (result.getTestStage() == null || result.getTestStage().getSteps() == null) {
            return "";
        }
        return result.getTestStage().getSteps().stream()
                .map(step -> step.getName())
                .collect(Collectors.joining(" | "));
    }

    private String describeStageAttachments(final TestResult result) {
        if (result.getTestStage() == null || result.getTestStage().getAttachments() == null) {
            return "";
        }
        return result.getTestStage().getAttachments().stream()
                .map(attachment -> attachment.getName() + "=" + attachment.getUid())
                .collect(Collectors.joining(", "));
    }

    private String contentType(final String fileName) {
        return fileName.endsWith(".txt") ? "text/plain" : "application/xml";
    }

    private String extension(final String fileName) {
        return fileName.endsWith(".txt") ? ".txt" : ".xml";
    }
}
