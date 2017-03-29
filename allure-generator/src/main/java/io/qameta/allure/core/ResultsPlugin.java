package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.JacksonMapperContext;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.entity.TestCaseResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class ResultsPlugin implements Aggregator {

    @Override
    public void aggregate(final ReportConfiguration configuration,
                          final List<LaunchResults> launches,
                          final Path outputDirectory) throws IOException {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path testCasesFolder = Files.createDirectories(outputDirectory.resolve("data/test-cases"));
        final List<TestCaseResult> results = launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .collect(Collectors.toList());
        for (TestCaseResult result : results) {
            final Path file = testCasesFolder.resolve(result.getSource());
            try (OutputStream os = Files.newOutputStream(file)) {
                context.getValue().writeValue(os, result);
            }
        }
    }
}
