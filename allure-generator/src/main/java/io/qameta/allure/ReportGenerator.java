package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportGenerator {

    private final Configuration configuration;

    public ReportGenerator(final Configuration configuration) {
        this.configuration = configuration;
    }

    public LaunchResults readResults(final Path resultsDirectory) {
        final DefaultResultsVisitor visitor = new DefaultResultsVisitor(configuration);
        configuration
                .getReaders()
                .forEach(reader -> reader.readResults(configuration, visitor, resultsDirectory));
        return visitor.getLaunchResults();
    }

    public void aggregate(final List<LaunchResults> results, final Path outputDirectory) throws IOException {
        for (Aggregator aggregator : configuration.getAggregators()) {
            aggregator.aggregate(configuration, results, outputDirectory);
        }
    }

    public void generate(final Path outputDirectory, final List<Path> resultsDirectories) throws IOException {
        generate(outputDirectory, resultsDirectories.stream());
    }

    public void generate(final Path outputDirectory, final Path... resultsDirectories) throws IOException {
        generate(outputDirectory, Stream.of(resultsDirectories));
    }

    private void generate(final Path outputDirectory, final Stream<Path> resultsDirectories) throws IOException {
        final List<LaunchResults> results = resultsDirectories
                .map(this::readResults)
                .collect(Collectors.toList());
        aggregate(results, outputDirectory);
    }

}
