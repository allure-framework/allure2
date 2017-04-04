package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
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
public class ResultsAggregator implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path testCasesFolder = Files.createDirectories(outputDirectory.resolve("data/test-cases"));
        final List<TestCaseResult> results = launchesResults.stream()
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
