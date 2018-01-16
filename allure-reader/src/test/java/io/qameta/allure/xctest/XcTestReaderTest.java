package io.qameta.allure.xctest;

import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.TestResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author charlie (Dmitry Baev).
 */
public class XcTestReaderTest {

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
    public void shouldParseResults() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path file = resultsDirectory.resolve("sample.plist");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("xctestdata/sample.plist")) {
            Files.copy(is, file);
        }

        new XcTestReader().readResults(visitor, file);

        verify(visitor, times(14))
                .visitTestResult(any(TestResult.class));
    }
}