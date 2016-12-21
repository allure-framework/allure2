package io.qameta.allure.testrun;

import io.qameta.allure.ResultAggregator;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.qameta.allure.utils.ListUtils.compareBy;
import static io.qameta.allure.utils.ListUtils.computeIfAbsent;

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
