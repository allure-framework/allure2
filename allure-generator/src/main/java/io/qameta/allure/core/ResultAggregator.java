package io.qameta.allure.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qameta.allure.Aggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.entity.TestResult;
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
public class ResultAggregator implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultAggregator.class);

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
            writeResult(mapper, directory, testResult);
            writeResultExecution(service, mapper, directory, testResult);
            writeResultAttachments(service, mapper, directory, testResult);
            writeResultRetries(service, mapper, directory, testResult);
        });
    }

    private void writeResult(final ObjectMapper mapper, final Path directory, final TestResult testResult) {
        final Path file = directory.resolve(String.format("%d.json", testResult.getId()));
        try (OutputStream os = Files.newOutputStream(file)) {
            mapper.writeValue(os, testResult);
        } catch (IOException e) {
            LOGGER.error("Could not write result", e);
        }
    }

    private void writeResultExecution(final TestResultService service, final ObjectMapper mapper,
                                      final Path directory, final TestResult testResult) {
        final TestResultExecution found = service.findExecution(testResult.getId())
                .orElseGet(TestResultExecution::new);

        final Path file = directory.resolve(String.format("%d-execution.json", testResult.getId()));
        try (OutputStream os = Files.newOutputStream(file)) {
            mapper.writeValue(os, found);
        } catch (IOException e) {
            LOGGER.error("Could not write execution for result", e);
        }
    }

    private void writeResultAttachments(final TestResultService service, final ObjectMapper mapper,
                                        final Path directory, final TestResult testResult) {
        final Path file = directory.resolve(String.format("%d-attachments.json", testResult.getId()));
        try (OutputStream os = Files.newOutputStream(file)) {
            mapper.writeValue(os, service.findAttachments(testResult.getId()));
        } catch (IOException e) {
            LOGGER.error("Could not write attachments for result", e);
        }
    }

    private void writeResultRetries(final TestResultService service, final ObjectMapper mapper,
                                    final Path directory, final TestResult testResult) {
        final Path file = directory.resolve(String.format("%d-retries.json", testResult.getId()));
        try (OutputStream os = Files.newOutputStream(file)) {
            mapper.writeValue(os, service.findRetries(testResult.getId()));
        } catch (IOException e) {
            LOGGER.error("Could not write attachments for result", e);
        }
    }

}
