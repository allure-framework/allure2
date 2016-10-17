package org.allurefw.report.testrun;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.allurefw.report.TestRunReader;
import org.allurefw.report.entity.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTestRunReader implements TestRunReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTestRunReader.class);

    private final ObjectMapper mapper;

    public DefaultTestRunReader() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public TestRun readTestRun(Path source) {
        Path file = source.resolve("test-run.json");
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                return mapper.readValue(is, TestRun.class);
            } catch (IOException e) {
                LOGGER.error("Could not read test run from {}", file, e);
            }
        }
        return new TestRun().withName(source.getFileName().toString());
    }

}
