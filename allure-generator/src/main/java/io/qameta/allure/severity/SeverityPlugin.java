package io.qameta.allure.severity;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;

import java.nio.file.Path;
import java.util.List;

import static io.qameta.allure.entity.LabelName.SEVERITY;

/**
 * Plugin that adds severity information to tests results.
 *
 * @since 2.0
 */
public class SeverityPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .forEach(this::setSeverityLevel);

    }

    private void setSeverityLevel(final TestResult result) {
        final SeverityLevel severityLevel = result.findOneLabel(SEVERITY)
                .flatMap(SeverityLevel::fromValue)
                .orElse(SeverityLevel.NORMAL);
        result.setExtraBlock("severity", severityLevel);
    }
}
