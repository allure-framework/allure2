package io.qameta.allure.xctest;

import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.TestResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
public class XcTestPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Configuration configuration;

    private ResultsVisitor visitor;

    @Before
    public void setUp() throws Exception {
        configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class)).thenReturn(new JacksonContext());
        visitor = mock(ResultsVisitor.class);
    }

    @Test
    public void shouldParseResults() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sample.plist")) {
            Files.copy(is, resultsDirectory.resolve("sample.plist"));
        }

        new XcTestPlugin().readResults(configuration, visitor, resultsDirectory);

        verify(visitor, times(14))
                .visitTestResult(any(TestResult.class));
    }
}