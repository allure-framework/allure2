package io.qameta.allure.severity;

import io.qameta.allure.Processor;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.SeverityLevel;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class SeverityProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeverityProcessor.class);

    @Override
    public void process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        Optional<String> severity = result.findOne(LabelName.SEVERITY);
        result.addExtraBlock("severity", severity.isPresent()
                ? getSeverity(severity.get())
                : SeverityLevel.NORMAL
        );
    }

    public SeverityLevel getSeverity(String value) {
        try {
            return SeverityLevel.fromValue(value);
        } catch (Exception e) {
            LOGGER.error("Unknown severity level {}", value, e);
            return null;
        }
    }
}
