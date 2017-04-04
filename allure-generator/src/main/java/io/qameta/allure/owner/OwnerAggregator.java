package io.qameta.allure.owner;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class OwnerAggregator implements Aggregator {

    private static final String OWNER_BLOCK_NAME = "owner";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .forEach(this::setOwner);
    }

    private void setOwner(final TestCaseResult result) {
        result.findOne(LabelName.OWNER)
                .ifPresent(owner -> result.addExtraBlock(OWNER_BLOCK_NAME, owner));
    }
}
