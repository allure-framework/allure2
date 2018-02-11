package io.qameta.allure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qameta.allure.service.TestResultService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Common json aggregator.
 */
public abstract class AbstractJsonAggregator implements Aggregator {

    private final String location;

    private final String fileName;

    protected AbstractJsonAggregator(final String fileName) {
        this("data", fileName);
    }

    protected AbstractJsonAggregator(final String location, final String fileName) {
        this.location = location;
        this.fileName = fileName;
    }

    @Override
    public void aggregate(final ReportContext context, final TestResultService service,
                          final Path outputDirectory) throws IOException {
        final ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final Path dataFolder = Files.createDirectories(outputDirectory.resolve(this.location));
        final Path dataFile = dataFolder.resolve(this.fileName);
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            mapper.writeValue(os, getData(context, service));
        }
    }

    protected abstract Object getData(final ReportContext context, TestResultService service);
}
