package io.qameta.allure.trx;

import io.qameta.allure.Issue;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author charlie (Dmitry Baev).
 */
public class TrxPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ResultsVisitor visitor;

    @Before
    public void setUp() throws Exception {
        visitor = mock(ResultsVisitor.class);
    }

    @Test
    public void shouldParseResults() throws Exception {
        processFile(
                "trxdata/sample.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(4)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(4)
                .extracting(TestResult::getName, TestResult::getStatus, TestResult::getDescription)
                .containsExactlyInAnyOrder(
                        tuple("AddingSeveralNumbers_40", TestStatus.PASSED, "Adding several numbers"),
                        tuple("AddingSeveralNumbers_60", TestStatus.PASSED, "Adding several numbers"),
                        tuple("AddTwoNumbers", TestStatus.PASSED, "Add two numbers"),
                        tuple("FailToAddTwoNumbers", TestStatus.FAILED, "Fail to add two numbers")
                );

        assertThat(captor.getAllValues())
                .extracting(result -> result.findOneLabel(LabelName.RESULT_FORMAT))
                .extracting(Optional::get)
                .containsOnly(TrxReader.TRX_RESULTS_FORMAT);

    }

    @Issue("596")
    @Test
    public void shouldParseErrorInfo() throws Exception {
        processFile(
                "trxdata/gh-596.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getMessage, TestResult::getTrace)
                .containsExactly(tuple("Some message", "Some trace"));
    }

    private void processFile(final String resourceName, final String fileName) throws IOException {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path file = copyFile(resultsDirectory, resourceName, fileName);
        final TrxReader reader = new TrxReader();
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