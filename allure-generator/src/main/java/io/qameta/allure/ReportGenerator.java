package io.qameta.allure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportGenerator {

    private final ReportConfiguration configuration;

    public ReportGenerator(ReportConfiguration configuration) {
        this.configuration = configuration;
    }

    public LaunchResults readResults(ResultsVisitor visitor, Path resultsDirectory) {
        configuration
                .getReaders()
                .forEach(reader -> reader.readResults(configuration, visitor, resultsDirectory));
        return visitor.getLaunchResults();
    }

    public void process(List<LaunchResults> launchResults) {
        configuration
                .getProcessors()
                .forEach(processor -> processor.process(configuration, launchResults));
    }

    public void aggregate(List<LaunchResults> results, Path outputDirectory) throws IOException {
        for (Aggregator aggregator : configuration.getAggregators()) {
            aggregator.aggregate(configuration, results, outputDirectory);
        }
    }

    public void generate(Path outputDirectory, Path... resultsDirectories) throws IOException {
        final DefaultResultsVisitor visitor = new DefaultResultsVisitor();
        final List<LaunchResults> results = Stream.of(resultsDirectories)
                .map(path -> readResults(visitor, path))
                .collect(Collectors.toList());
        process(results);
        aggregate(results, outputDirectory);
    }

}
