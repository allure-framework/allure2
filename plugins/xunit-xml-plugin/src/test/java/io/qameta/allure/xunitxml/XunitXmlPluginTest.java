package io.qameta.allure.xunitxml;

import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
public class XunitXmlPluginTest {

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
    public void shouldCreateTest() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getName, TestResult::getHistoryId, TestResult::getStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("passedTest", "Some test", Status.PASSED)
                );
    }

    @Test
    public void shouldSetTime() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getTime)
                .extracting(Time::getDuration)
                .containsExactlyInAnyOrder(44L);
    }

    @Test
    public void shouldSetLabels() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .extracting(Label::getName, Label::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("suite", "org.example.XunitTest"),
                        Tuple.tuple("testClass", "org.example.XunitTest"),
                        Tuple.tuple("package", "org.example.XunitTest")
                );
    }

    @Test
    public void shouldSetFullName() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getFullName)
                .containsExactlyInAnyOrder(
                       "Some test"
                );
    }

    @Test
    public void shouldSetStatusDetails() throws Exception {
        process(
                "xunitdata/failed-test.xml",
                "failed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getStatusDetails)
                .extracting(StatusDetails::getMessage, StatusDetails::getTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("Assert.True() Failure\\r\\nExpected: True\\r\\nActual:   False", "FAILED-TRACE")
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
        XunitXmlPlugin reader = new XunitXmlPlugin();

        reader.readResults(configuration, visitor, resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }

}