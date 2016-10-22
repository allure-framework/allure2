package org.allurefw.report.history;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.allurefw.report.TestRunDetailsReader;
import org.allurefw.report.entity.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

import static org.allurefw.report.history.HistoryPlugin.HISTORY_JSON;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryReader implements TestRunDetailsReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryReader.class);

    public static final TypeReference<Map<String, HistoryData>> HISTORY_TYPE =
            new TypeReference<Map<String, HistoryData>>() {
            };


    private final ObjectMapper mapper;

    @Inject
    public HistoryReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Consumer<TestRun> readDetails(Path source) {
        return testRun -> {
            Path file = source.resolve(HISTORY_JSON);
            if (Files.exists(file)) {
                try (InputStream is = Files.newInputStream(file)) {
                    Map<String, HistoryData> history = mapper.readValue(is, HISTORY_TYPE);
                    testRun.addExtraBlock("history", history);
                } catch (IOException e) {
                    LOGGER.error("Could not read history file {}", file, e);
                }
            }
        };
    }
}
