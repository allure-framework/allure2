package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.entity.TestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that stores test results to report data folder.
 *
 * @since 2.0
 */
public class TestsResultsPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path testCasesFolder = Files.createDirectories(
                outputDirectory.resolve(Constants.DATA_DIR).resolve("test-cases")
        );
        final List<TestResult> results = launchesResults.stream()
                .flatMap(launch -> launch.getAllResults().stream())
                .collect(Collectors.toList());
        for (TestResult result : results) {
            final Path file = testCasesFolder.resolve(result.getSource());
            try (OutputStream os = Files.newOutputStream(file)) {
                context.getValue().writeValue(os, result);
            }
        }
    }
}
