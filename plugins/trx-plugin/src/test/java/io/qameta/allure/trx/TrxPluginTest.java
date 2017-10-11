package io.qameta.allure.trx;

import io.qameta.allure.Issue;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
public class TrxPluginTest {

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
    public void shouldParseResults() throws Exception {
        process(
                "trxdata/sample.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(4)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(4)
                .extracting(TestResult::getName, TestResult::getStatus, TestResult::getDescription)
                .containsExactlyInAnyOrder(
                        tuple("AddingSeveralNumbers_40", Status.PASSED, "Adding several numbers"),
                        tuple("AddingSeveralNumbers_60", Status.PASSED, "Adding several numbers"),
                        tuple("AddTwoNumbers", Status.PASSED, "Add two numbers"),
                        tuple("FailToAddTwoNumbers", Status.FAILED, "Fail to add two numbers")
                );

        assertThat(captor.getAllValues())
                .extracting(result -> result.findOneLabel(LabelName.RESULT_FORMAT))
                .extracting(Optional::get)
                .containsOnly(TrxPlugin.TRX_RESULTS_FORMAT);

    }

    @Issue("596")
    @Test
    public void shouldParseErrorInfo() throws Exception {
        process(
                "trxdata/gh-596.trx",
                "sample.trx"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TestResult::getStatusDetails)
                .extracting(StatusDetails::getMessage, StatusDetails::getTrace)
                .containsExactly(tuple("Some message", "Some trace"));
    }

    private void process(String... strings) throws IOException {
        Path resultsDirectory = folder.newFolder().toPath();
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        TrxPlugin reader = new TrxPlugin();
        reader.readResults(configuration, visitor, resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}