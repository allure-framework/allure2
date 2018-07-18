package io.qameta.allure.owner;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;

import java.nio.file.Path;
import java.util.List;

/**
 * Plugin that adds owner information to test results.
 *
 * @since 2.0
 */
public class OwnerPlugin implements Aggregator {

    public static final String OWNER_BLOCK_NAME = "owner";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .forEach(this::setOwner);
    }

    private void setOwner(final TestResult result) {
        result.findOneLabel(LabelName.OWNER)
                .ifPresent(owner -> result.addExtraBlock(OWNER_BLOCK_NAME, owner));
    }
}
