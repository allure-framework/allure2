package io.qameta.allure.junit;

import io.qameta.allure.Issue;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestLabel;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.entity.TestStatus;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitReaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    private ResultsVisitor visitor;

    @Before
    public void setUp() throws Exception {
        visitor = mock(ResultsVisitor.class);

        doAnswer(invocation -> {
            final TestResult firstArgument = invocation.getArgument(0);
            firstArgument.setId(ThreadLocalRandom.current().nextLong());
            return firstArgument;
        }).when(visitor).visitTestResult(any(TestResult.class));
    }

    @Test
    public void shouldReadJunitResults() throws Exception {
        processFile(
                "junitdata/TEST-org.allurefw.report.junit.JunitTestResultsTest.xml",
                "TEST-org.allurefw.report.junit.JunitTestResultsTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(5)).visitTestResult(captor.capture());


        assertThat(captor.getAllValues())
                .hasSize(5);

        List<TestResult> failed = filterByStatus(captor.getAllValues(), TestStatus.FAILED);
        List<TestResult> skipped = filterByStatus(captor.getAllValues(), TestStatus.SKIPPED);
        List<TestResult> passed = filterByStatus(captor.getAllValues(), TestStatus.PASSED);

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

    @Ignore("Attachment file reader?")
    @Test
    public void shouldAddLogAsAttachment() throws Exception {
        final Attachment hey = new Attachment().setUid("some-id");
        when(visitor.visitAttachmentFile(any())).thenReturn(hey);
        processFile(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<Path> attachmentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(visitor, times(1)).visitAttachmentFile(attachmentCaptor.capture());

        assertThat(attachmentCaptor.getValue())
                .isRegularFile()
                .hasContent("some-test-log");

        final ArgumentCaptor<TestResultExecution> executionCaptor = ArgumentCaptor.forClass(TestResultExecution.class);
        verify(visitor, times(1)).visitTestResultExecution(anyLong(), executionCaptor.capture());

        assertThat(executionCaptor.getValue().getAttachments())
                .describedAs("Should add an attachment")
                .hasSize(1)
                .describedAs("Attachment should has right id and name")
                .extracting(AttachmentLink::getName, AttachmentLink::getFileName)
                .containsExactly(Tuple.tuple("System out", "some-id"));
    }

    @Test
    public void shouldAddLabels() throws Exception {
        processFile(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .extracting(TestLabel::getName, TestLabel::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LabelName.SUITE.value(), "test.SampleTest"),
                        Tuple.tuple(LabelName.PACKAGE.value(), "test.SampleTest"),
                        Tuple.tuple(LabelName.TEST_CLASS.value(), "test.SampleTest"),
                        Tuple.tuple(LabelName.RESULT_FORMAT.value(), JunitReader.JUNIT_RESULTS_FORMAT)
                );
    }

    @Test
    public void shouldSkipInvalidXml() throws Exception {
        processFile(
                "junitdata/invalid.xml", "sample-testsuite.xml"
        );

        verify(visitor, times(0)).visitTestResult(any());
    }

    @Test
    public void shouldProcessTestsWithRetry() throws Exception {
        processFile(
                "junitdata/TEST-test.RetryTest.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(4)).visitTestResult(captor.capture());

        final List<TestResult> results = captor.getAllValues();
        assertThat(results)
                .extracting(TestResult::getName, TestResult::getStatus, TestResult::isHidden, TestResult::getHistoryKey)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("searchTest", TestStatus.BROKEN, false, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", TestStatus.BROKEN, true, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", TestStatus.BROKEN, true, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", TestStatus.FAILED, true, "my.company.tests.SearchTest:my.company.tests.SearchTest#searchTest")
                );

        assertThat(results)
                .extracting(TestResult::getMessage, TestResult::getTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("message-root", "trace-root"),
                        Tuple.tuple("message-retried-1", "trace-retried-1"),
                        Tuple.tuple("message-retried-2", "trace-retried-2"),
                        Tuple.tuple("message-retried-3", "trace-retried-3")
                );
    }

    @Test
    public void shouldReadCdataMessage() throws Exception {
        processFile(
                "junitdata/TEST-test.CdataMessage.xml", "TEST-test.SampleTest.xml"
        );


        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getMessage, TestResult::getTrace)
                .containsExactlyInAnyOrder(
                        tuple("some-message", "some-trace")
                );

    }

    @Issue("532")
    @Test
    public void shouldParseSuitesTag() throws Exception {
        processFile(
                "junitdata/testsuites.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(3)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getName)
                .containsExactlyInAnyOrder(
                        "should default path to an empty string",
                        "should default consolidate to true",
                        "should default useDotNotation to true"
                );
    }

    @Test
    public void shouldProcessTimestampIfPresent() throws Exception {
        processFile(
                "junitdata/with-timestamp.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getDuration)
                .containsExactly(1051L);

        assertThat(captor.getAllValues())
                .extracting(TestResult::getStart)
                .isNotNull();

        assertThat(captor.getAllValues())
                .extracting(TestResult::getStop)
                .isNotNull();
    }

    @Test
    public void shouldUseSuiteNameIfPresent() throws Exception {
        processFile(
                "junitdata/with-timestamp.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .flatExtracting(TestResult::getLabels)
                .filteredOn("name", "suite")
                .extracting(TestLabel::getValue)
                .containsExactly("LocalSuiteIDOL");

    }

    @Test
    public void shouldUseHostnameIfPresent() throws Exception {
        processFile(
                "junitdata/with-timestamp.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .flatExtracting(TestResult::getLabels)
                .filteredOn("name", "host")
                .extracting(TestLabel::getValue)
                .containsExactly("cbgtalosbld02");

    }

    private void processFile(final String resourceName, final String fileName) throws IOException {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path file = copyFile(resultsDirectory, resourceName, fileName);
        final JunitReader reader = new JunitReader();
        reader.readResultFile(visitor, file);
    }

    private Path copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            final Path file = dir.resolve(fileName);
            Files.copy(is, file);
            return file;
        }
    }

    private List<TestResult> filterByStatus(List<TestResult> results, TestStatus status) {
        return results.stream()
                .filter(item -> status.equals(item.getStatus()))
                .collect(Collectors.toList());
    }
}