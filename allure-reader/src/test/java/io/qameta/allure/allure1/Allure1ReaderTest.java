package io.qameta.allure.allure1;

import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.TestParameter;
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

import static io.qameta.allure.entity.TestStatus.FAILED;
import static io.qameta.allure.entity.TestStatus.PASSED;
import static io.qameta.allure.entity.TestStatus.UNKNOWN;
import static org.allurefw.allure1.AllureUtils.generateTestSuiteXmlName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure1ReaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    protected ResultsVisitor visitor;

    @Before
    public void setUp() {
        visitor = mock(ResultsVisitor.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProcessEmptyOrNullStatus() throws Exception {
        processFile("allure1data/empty-status-testsuite.xml", generateTestSuiteXmlName());

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(4))
                .visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting("name", "status")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("testOne", UNKNOWN),
                        Tuple.tuple("testTwo", PASSED),
                        Tuple.tuple("testThree", FAILED),
                        Tuple.tuple("testFour", UNKNOWN)
                );
    }

    @Test
    public void shouldReadTestSuiteXml() throws Exception {
        processFile("allure1data/sample-testsuite.xml", generateTestSuiteXmlName());
        verify(visitor, times(4))
                .visitTestResult(any());
    }

    @Test
    public void shouldExcludeDuplicatedParams() throws Exception {
        processFile("allure1data/duplicated-params.xml", generateTestSuiteXmlName());

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1))
                .visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .flatExtracting(TestResult::getParameters)
                .extracting(TestParameter::getName, TestParameter::getValue)
                .containsExactlyInAnyOrder(
                        tuple("name", "value"),
                        tuple("name2", "value"),
                        tuple("name", "value2"),
                        tuple("name2", "value2")
                );
    }

    private void processFile(final String resourceName, final String fileName) throws IOException {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path file = copyFile(resultsDirectory, resourceName, fileName);
        final Allure1Reader reader = new Allure1Reader();
        reader.readResultFile(visitor, file);
    }

    private Path copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            final Path file = dir.resolve(fileName);
            Files.copy(is, file);
            return file;
        }
    }

}