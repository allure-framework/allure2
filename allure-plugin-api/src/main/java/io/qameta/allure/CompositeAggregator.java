package io.qameta.allure;

import io.qameta.allure.service.TestResultService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Composite aggregator extension. Can be used to process the list of aggregator.
 *
 * @since 2.0
 */
public class CompositeAggregator implements Aggregator {

    private final Set<Aggregator> aggregators;

    public CompositeAggregator(final Set<Aggregator> aggregators) {
        this.aggregators = aggregators;
    }

    @Override
    public void aggregate(final ReportContext context, final TestResultService testResultService,
                          final Path outputDirectory) throws IOException {
        for (Aggregator aggregator : aggregators) {
            aggregator.aggregate(context, testResultService, outputDirectory);
        }
    }
}
