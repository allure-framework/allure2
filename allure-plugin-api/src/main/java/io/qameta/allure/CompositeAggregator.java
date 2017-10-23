package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Composite aggregator extension. Can be used to process the list of aggregator.
 *
 * @since 2.0
 */
public class CompositeAggregator implements Aggregator {

    private final List<Aggregator> aggregators;

    public CompositeAggregator(final List<Aggregator> aggregators) {
        this.aggregators = aggregators;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        for (Aggregator aggregator : aggregators) {
            aggregator.aggregate(configuration, launchesResults, outputDirectory);
        }
    }
}
