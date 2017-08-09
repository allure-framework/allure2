package io.qameta.allure.junitxml;

import io.qameta.allure.Issue;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestResult;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class JunitXmlPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Configuration configuration;

    private ResultsVisitor visitor;

    @Before
    public void setUp() throws Exception {
        configuration = mock(Configuration.class);
        when(configuration.requireContext(RandomUidContext.class)).thenReturn(new RandomUidContext());
        visitor = mock(ResultsVisitor.class);
    }

    @Test
    public void shouldReadJunitResults() throws Exception {
        process(
                "junitdata/TEST-org.allurefw.report.junit.JunitTestResultsTest.xml",
                "TEST-org.allurefw.report.junit.JunitTestResultsTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(5)).visitTestResult(captor.capture());


        assertThat(captor.getAllValues())
                .hasSize(5);

        List<TestResult> failed = filterByStatus(captor.getAllValues(), Status.FAILED);
        List<TestResult> skipped = filterByStatus(captor.getAllValues(), Status.SKIPPED);
        List<TestResult> passed = filterByStatus(captor.getAllValues(), Status.PASSED);

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

    @Test
    public void shouldAddLogAsAttachment() throws Exception {
        final Attachment hey = new Attachment().setUid("some-uid");
        when(visitor.visitAttachmentFile(any())).thenReturn(hey);
        process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml",
                "junitdata/test.SampleTest.txt", "test.SampleTest.txt"
        );

        final ArgumentCaptor<Path> attachmentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(visitor, times(1)).visitAttachmentFile(attachmentCaptor.capture());

        assertThat(attachmentCaptor.getValue())
                .isRegularFile()
                .hasContent("some-test-log");

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        final StageResult testStage = captor.getValue().getTestStage();
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

    @Test
    public void shouldAddLabels() throws Exception {
        process(
                "junitdata/TEST-test.SampleTest.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
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

    @Test
    public void shouldSkipInvalidXml() throws Exception {
        process(
                "junitdata/invalid.xml", "sample-testsuite.xml"
        );

        verify(visitor, times(0)).visitTestResult(any());
    }

    @Test
    public void shouldProcessTestsWithRetry() throws Exception {
        process(
                "junitdata/TEST-test.RetryTest.xml", "TEST-test.SampleTest.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(4)).visitTestResult(captor.capture());

        final List<TestResult> results = captor.getAllValues();
        assertThat(results)
                .extracting(TestResult::getName, TestResult::getStatus, TestResult::isHidden, TestResult::getHistoryId)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("searchTest", Status.BROKEN, false, "my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", Status.BROKEN, true, "my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", Status.BROKEN, true, "my.company.tests.SearchTest#searchTest"),
                        Tuple.tuple("searchTest", Status.FAILED, true, "my.company.tests.SearchTest#searchTest")
                );

        assertThat(results)
                .extracting(TestResult::getStatusDetails)
                .filteredOn(Objects::nonNull)
                .hasSize(4)
                .extracting(StatusDetails::getMessage, StatusDetails::getTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("message-root", "trace-root"),
                        Tuple.tuple("message-retried-1", "trace-retried-1"),
                        Tuple.tuple("message-retried-2", "trace-retried-2"),
                        Tuple.tuple("message-retried-3", "trace-retried-3")
                );
    }

    @Test
    public void shouldReadCdataMessage() throws Exception {
        process(
                "junitdata/TEST-test.CdataMessage.xml", "TEST-test.SampleTest.xml"
        );


        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getStatusDetails)
                .extracting(StatusDetails::getMessage, StatusDetails::getTrace)
                .containsExactlyInAnyOrder(
                        tuple("some-message", "some-trace")
                );

    }

    @Issue("532")
    @Test
    public void shouldParseSuitesTag() throws Exception {
        process(
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

    private void process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        JunitXmlPlugin reader = new JunitXmlPlugin();

        reader.readResults(configuration, visitor, resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }

    private List<TestResult> filterByStatus(List<TestResult> testCases, Status status) {
        return testCases.stream()
                .filter(item -> status.equals(item.getStatus()))
                .collect(Collectors.toList());
    }
}