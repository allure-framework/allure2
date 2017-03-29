package io.qameta.allure.severity;

import io.qameta.allure.LaunchResults;
import io.qameta.allure.Processor;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.entity.SeverityLevel;
import io.qameta.allure.entity.TestCaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.qameta.allure.entity.LabelName.SEVERITY;

/**
 * @author charlie (Dmitry Baev).
 */
public class SeverityProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeverityProcessor.class);

    @Override
    public void process(final ReportConfiguration configuration, final List<LaunchResults> launches) {
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
