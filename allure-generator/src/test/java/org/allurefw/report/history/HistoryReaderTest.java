package org.allurefw.report.history;

import com.google.inject.Guice;
import org.allurefw.report.entity.TestRun;
import org.allurefw.report.jackson.JacksonMapperModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryReaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Consumer<TestRun> consumer;

    @Before
    public void setUp() throws Exception {
        Path dir = folder.newFolder().toPath();
        Path history = dir.resolve(HistoryPlugin.HISTORY_JSON);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("history.json")) {
            Files.copy(is, history);
        }
        HistoryReader reader = Guice.createInjector(new JacksonMapperModule())
                .getInstance(HistoryReader.class);
        consumer = reader.readDetails(dir);
    }

    @Test
    public void shouldReadHistory() throws Exception {
        TestRun mock = mock(TestRun.class);
        consumer.accept(mock);
        verify(mock).addExtraBlock(eq(HistoryPlugin.HISTORY), any(Map.class));
    }
}