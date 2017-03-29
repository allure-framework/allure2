package io.qameta.allure.severity;

import io.qameta.allure.Configuration;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.Plugin;
import io.qameta.allure.entity.SeverityLevel;
import io.qameta.allure.entity.TestCaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

import static io.qameta.allure.entity.LabelName.SEVERITY;

/**
 * @author charlie (Dmitry Baev).
 */
public class SeverityPlugin implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeverityPlugin.class);

    @Override
    public void process(final Configuration configuration,
                        final List<LaunchResults> launches,
                        final Path outputDirectory) {
        launches.stream()
                .flatMap(results -> results.getResults().stream())
                .forEach(this::setSeverityLevel);

    }

    private void setSeverityLevel(final TestCaseResult result) {
        final SeverityLevel severityLevel = result.findOne(SEVERITY)
                .map(this::getSeverity)
                .orElse(SeverityLevel.NORMAL);
        result.addExtraBlock("severity", severityLevel);
    }

    private SeverityLevel getSeverity(final String value) {
        try {
            return SeverityLevel.fromValue(value);
        } catch (Exception e) {
            LOGGER.error("Unknown severity level {}", value, e);
            return null;
        }
    }
}
