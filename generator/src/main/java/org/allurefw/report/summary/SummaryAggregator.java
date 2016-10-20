package org.allurefw.report.summary;

import org.allurefw.report.ResultAggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author charlie (Dmitry Baev).
 */
public class SummaryAggregator implements ResultAggregator<SummaryData> {

    @Override
    public Supplier<SummaryData> supplier(TestRun testRun, TestCase testCase) {
        return SummaryData::new;
    }

    @Override
    public Consumer<SummaryData> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return summaryData -> {
            String name = summaryData.getTestRuns().stream()
                    .filter(item -> Objects.equals(testRun.getName(), item))
                    .findAny()
                    .orElseGet(() -> {
                        summaryData.getTestRuns().add(testRun.getName());
                        return testRun.getName();
                    });
            summaryData.setReportName(name);
            summaryData.updateStatistic(result);
            summaryData.updateTime(result);
        };
    }
}
