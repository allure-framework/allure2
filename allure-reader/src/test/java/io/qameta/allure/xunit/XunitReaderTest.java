package io.qameta.allure.xunit;

import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestLabel;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author charlie (Dmitry Baev).
 */
@RunWith(Theories.class)
public class XunitReaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ResultsVisitor visitor;

    @Before
    public void setUp() throws Exception {
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
        processFile(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getName, TestResult::getHistoryId, TestResult::getStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("passedTest", "Some test", TestStatus.PASSED)
                );
    }

    @Test
    public void shouldSetTime() throws Exception {
        processFile(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getDuration)
                .containsExactlyInAnyOrder(44L);
    }

    @Test
    public void shouldSetLabels() throws Exception {
        processFile(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .extracting(TestLabel::getName, TestLabel::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LabelName.SUITE.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.PACKAGE.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.TEST_CLASS.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.RESULT_FORMAT.value(), XunitReader.XUNIT_RESULTS_FORMAT)
                );
    }

    @Test
    public void shouldSetFullName() throws Exception {
        processFile(
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
        processFile(
                "xunitdata/framework-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .filteredOn(label -> label.getName().equals(LabelName.FRAMEWORK.value()))
                .extracting(TestLabel::getValue)
                .containsExactly("junit");
    }

    @Theory
    public void shouldSetStatusDetails(String[] inputs) throws Exception {
        Assume.assumeTrue(inputs.length == 4);
        processFile(
                inputs[0],
                inputs[1]
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getMessage, TestResult::getTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(inputs[2], inputs[3])
                );
    }

    private void processFile(final String resourceName, final String fileName) throws IOException {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path file = copyFile(resultsDirectory, resourceName, fileName);
        final XunitReader reader = new XunitReader();
        reader.readResults(visitor, file);
    }

    private Path copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            final Path file = dir.resolve(fileName);
            Files.copy(is, file);
            return file;
        }
    }

}