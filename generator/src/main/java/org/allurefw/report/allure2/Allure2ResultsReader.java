package org.allurefw.report.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.model.TestCaseResult;
import org.allurefw.report.ResultsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.allurefw.report.ReportApiUtils.listFiles;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure2ResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2ResultsReader.class);

    private final ObjectMapper mapper;

    public Allure2ResultsReader() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<org.allurefw.report.entity.TestCaseResult> readResults(Path source) {
        return listFiles(source, "*-testcase.json")
                .map(this::readTestCaseResult)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(result -> (org.allurefw.report.entity.TestCaseResult) null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Optional<TestCaseResult> readTestCaseResult(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return Optional.of(mapper.readValue(is, TestCaseResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {}: {}", file, e);
        }
        return Optional.empty();
    }

}
