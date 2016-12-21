package io.qameta.allure.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.TestRunReader;
import io.qameta.allure.entity.TestRun;
import io.qameta.allure.entity.TestRunInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.qameta.allure.ReportApiUtils.generateUid;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTestRunReader implements TestRunReader {

    public static final String TESTRUN_FILE_NAME = "testrun.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTestRunReader.class);

    private final ObjectMapper mapper;

    public DefaultTestRunReader() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public TestRun readTestRun(Path source) {
        Path file = source.resolve(TESTRUN_FILE_NAME);
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                TestRunInfo info = mapper.readValue(is, TestRunInfo.class);
                TestRun testRun = new TestRun().withName(info.getName());
                testRun.setTime(info.getStart(), info.getStop());
                testRun.setUid(generateUid());
                return testRun;
            } catch (IOException e) {
                LOGGER.error("Could not read test run from {}", file, e);
            }
        }
        return new TestRun()
                .withName("Allure Report")
                .withUid(generateUid());
    }
}
