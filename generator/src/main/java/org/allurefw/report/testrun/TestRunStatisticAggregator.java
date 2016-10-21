package org.allurefw.report.testrun;

import org.allurefw.report.ResultAggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.allurefw.report.utils.ListUtils.compareBy;
import static org.allurefw.report.utils.ListUtils.computeIfAbsent;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestRunStatisticAggregator implements ResultAggregator<List<TestRunStatistic>> {

    @Override
    public Supplier<List<TestRunStatistic>> supplier(TestRun testRun, TestCase testCase) {
        return ArrayList::new;
    }

    @Override
    public Consumer<List<TestRunStatistic>> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return stats -> {
            Predicate<TestRunStatistic> predicate = compareBy(TestRunStatistic::getUid, testRun::getUid);
            Supplier<TestRunStatistic> supplier = () ->
                    new TestRunStatistic().withUid(testRun.getUid()).withName(testRun.getName());

            computeIfAbsent(stats, predicate, supplier)
                    .updateStatistic(result);
        };
    }
}
