package org.allurefw.report.junit;

import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.junit.JunitPlugin;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitTestResultsTest {

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
        final Attachment hey = new Attachment().withUid("some-uid");
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
    public void shouldSkipInvalidXml() throws Exception {
        process(
                "junitdata/invalid.xml", "sample-testsuite.xml"
        );

        verify(visitor, times(0)).visitTestResult(any());
    }

    private void process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        JunitPlugin reader = new JunitPlugin();

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