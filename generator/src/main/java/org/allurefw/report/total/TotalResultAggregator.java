package org.allurefw.report.total;

import org.allurefw.report.ResultAggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author charlie (Dmitry Baev).
 */
public class TotalResultAggregator implements ResultAggregator<TotalData> {

    @Override
    public Supplier<TotalData> supplier(TestRun testRun, TestCase testCase) {
        return TotalData::new;
    }

    @Override
    public Consumer<TotalData> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return totalData -> {
            totalData.updateStatistic(result);
            totalData.updateTime(result);
        };
    }
}
