package org.allurefw.report.testrun;

import org.allurefw.report.ResultAggregator;
import org.allurefw.report.entity.Statistic;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
            TestRunStatistic stat = stats.stream()
                    .filter(item -> Objects.equals(testRun.getName(), item.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        TestRunStatistic newItem = new TestRunStatistic();
                        newItem.setName(testRun.getName());
                        newItem.setStatistic(new Statistic());
                        stats.add(newItem);
                        return newItem;
                    });
            stat.getStatistic().update(result);
        };
    }
}
