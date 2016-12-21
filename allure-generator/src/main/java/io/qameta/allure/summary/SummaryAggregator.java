package io.qameta.allure.summary;

import io.qameta.allure.ResultAggregator;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

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
            boolean anyMatch = summaryData.getTestRuns().stream().anyMatch(testRun.getUid()::equals);
            if (!anyMatch) {
                summaryData.getTestRuns().add(testRun.getUid());
            }
            summaryData.setReportName(testRun.getName());
            summaryData.updateStatistic(result);
            summaryData.updateTime(result);
        };
    }
}
