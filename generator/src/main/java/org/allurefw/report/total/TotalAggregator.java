package org.allurefw.report.total;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author charlie (Dmitry Baev).
 */
public class TotalAggregator implements Aggregator<TotalData> {

    @Override
    public Supplier<TotalData> supplier() {
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
