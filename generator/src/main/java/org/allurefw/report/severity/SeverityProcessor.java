package org.allurefw.report.severity;

import org.allurefw.report.Processor;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.SeverityLevel;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;
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
