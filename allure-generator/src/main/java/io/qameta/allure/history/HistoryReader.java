package io.qameta.allure.history;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.TestRunDetailsReader;
import io.qameta.allure.entity.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

import static io.qameta.allure.history.HistoryPlugin.HISTORY_JSON;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryReader implements TestRunDetailsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryReader.class);

    //@formatter:off
    public static final TypeReference<Map<String, HistoryData>> HISTORY_TYPE =
        new TypeReference<Map<String, HistoryData>>() {};
    //@formatter:on

    private final ObjectMapper mapper;

    @Inject
    public HistoryReader(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Consumer<TestRun> readDetails(final Path source) {
        return testRun -> {
            final Path file = source.resolve(HISTORY_JSON);
            if (Files.exists(file)) {
                try (InputStream is = Files.newInputStream(file)) {
                    final Map<String, HistoryData> history = mapper.readValue(is, HISTORY_TYPE);
                    testRun.addExtraBlock("history", history);
                } catch (IOException e) {
                    LOGGER.error("Could not read history file {}", file, e);
                }
            }
        };
    }
}
