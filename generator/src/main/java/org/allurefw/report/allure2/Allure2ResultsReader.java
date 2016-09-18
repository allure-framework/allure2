package org.allurefw.report.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.model.TestCaseResult;
import org.allurefw.report.ResultsSource;
import org.allurefw.report.TestCaseResultsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure2ResultsReader implements TestCaseResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2ResultsReader.class);

    private final ObjectMapper mapper;

    public Allure2ResultsReader() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<org.allurefw.report.entity.TestCaseResult> readResults(ResultsSource source) {
        return source.getResultsByGlob("*-testcase.json").stream()
                .map(name -> readTestCaseResult(name, source))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(result -> (org.allurefw.report.entity.TestCaseResult) null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Optional<TestCaseResult> readTestCaseResult(String name, ResultsSource source) {
        try (InputStream is = source.getResult(name)) {
            return Optional.of(mapper.readValue(is, TestCaseResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {} from {}", name, source, e);
        }
        return Optional.empty();
    }

}
