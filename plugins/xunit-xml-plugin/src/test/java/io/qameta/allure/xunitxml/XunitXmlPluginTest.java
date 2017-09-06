package io.qameta.allure.xunitxml;

import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.assertj.core.groups.Tuple;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
@RunWith(Theories.class)
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

    @DataPoints
    public static String[][] input() {
        return new String[][]{
                {"xunitdata/failed-test.xml", "failed-test.xml",
                        String.format("%s%n", "Assert.True() Failure\\r\\nExpected: True\\r\\nActual:   False") +
                                "test output\\n", "FAILED-TRACE"},
                {"xunitdata/passed-test.xml", "passed-test.xml", "test output\\n", null}
        };
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
                        Tuple.tuple(LabelName.SUITE.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.PACKAGE.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.TEST_CLASS.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.RESULT_FORMAT.value(), XunitXmlPlugin.XUNIT_RESULTS_FORMAT)
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
    public void shouldSetFramework() throws Exception {
        process(
                "xunitdata/framework-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .filteredOn(label -> label.getName().equals(LabelName.FRAMEWORK.value()))
                .extracting(Label::getValue)
                .containsExactly("junit");
    }

    @Theory
    public void shouldSetStatusDetails(String[] inputs) throws Exception {
        Assume.assumeTrue(inputs.length == 4);
        process(
                inputs[0],
                inputs[1]
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getStatusDetails)
                .extracting(StatusDetails::getMessage, StatusDetails::getTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(inputs[2], inputs[3])
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