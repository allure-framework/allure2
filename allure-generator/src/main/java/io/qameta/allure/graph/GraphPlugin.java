package io.qameta.allure.graph;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Graph tab.
 *
 * @since 2.0
 */
public class GraphPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path graphFile = dataFolder.resolve("graph.json");

        final List<GraphData> data = launchesResults.stream()
                .flatMap(launch -> launch.getResults().stream())
                .map(this::createData)
                .collect(Collectors.toList());
        try (OutputStream os = Files.newOutputStream(graphFile)) {
            context.getValue().writeValue(os, data);
        }
    }

    private GraphData createData(final TestResult result) {
        return new GraphData()
                .setUid(result.getUid())
                .setName(result.getName())
                .setStatus(result.getStatus())
                .setTime(result.getTime())
                .setSeverity(result.getExtraBlock("severity"));
    }
}
