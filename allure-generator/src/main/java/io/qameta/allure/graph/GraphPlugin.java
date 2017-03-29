package io.qameta.allure.graph;

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
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class GraphPlugin implements Aggregator {

    @Override
    public void aggregate(ReportConfiguration configuration,
                          List<LaunchResults> launches,
                          Path outputDirectory) throws IOException {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path graphFile = dataFolder.resolve("graph.json");

        final List<GraphData> data = launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .map(this::createData)
                .collect(Collectors.toList());
        try (OutputStream os = Files.newOutputStream(graphFile)) {
            context.getValue().writeValue(os, data);
        }
    }

    private GraphData createData(TestCaseResult result) {
        return new GraphData()
                .withUid(result.getUid())
                .withName(result.getName())
                .withStatus(result.getStatus())
                .withTime(result.getTime())
                .withSeverity(result.getExtraBlock("severity"));
    }
}
