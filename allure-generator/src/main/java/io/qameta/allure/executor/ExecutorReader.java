package io.qameta.allure.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.TestRunDetailsReader;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTOR_BLOCK_NAME;
import static io.qameta.allure.executor.ExecutorPlugin.EXECUTOR_FILE_NAME;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorReader implements TestRunDetailsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorReader.class);

    private final ObjectMapper mapper;

    @Inject
    public ExecutorReader(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Consumer<TestRun> readDetails(final Path source) {
        return testRun -> {
            final Path file = source.resolve(EXECUTOR_FILE_NAME);
            if (Files.exists(file)) {
                try (InputStream is = Files.newInputStream(file)) {
                    final ExecutorInfo info = mapper.readValue(is, ExecutorInfo.class);
                    testRun.addExtraBlock(EXECUTOR_BLOCK_NAME, info);
                } catch (IOException e) {
                    LOGGER.error("Could not read executor file {}", file, e);
                }
            }
        };
    }
}
