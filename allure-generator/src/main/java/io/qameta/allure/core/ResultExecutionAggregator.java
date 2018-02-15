package io.qameta.allure.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qameta.allure.Aggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.service.TestResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class ResultExecutionAggregator implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultExecutionAggregator.class);

    @Override
    public void aggregate(final ReportContext context,
                          final TestResultService service,
                          final Path outputDirectory) throws IOException {
        final ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final Path directory = Files.createDirectories(outputDirectory.resolve("data").resolve("results"));
        service.findAll(true).forEach(testResult -> {
            final TestResultExecution found = service.findExecution(testResult.getId())
                    .orElseGet(TestResultExecution::new);

            final Path file = directory.resolve(String.format("%d-execution.json", testResult.getId()));
            try (OutputStream os = Files.newOutputStream(file)) {
                mapper.writeValue(os, found);
            } catch (IOException e) {
                LOGGER.error("Could not write execution for result", e);
            }
        });
    }
}
