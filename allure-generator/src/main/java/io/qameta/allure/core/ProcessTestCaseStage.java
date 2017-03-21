package io.qameta.allure.core;

import io.qameta.allure.TestCaseAggregator;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestRun;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessTestCaseStage {

    private final Map<String, TestCaseAggregator> aggregators;

    @Inject
    public ProcessTestCaseStage(final Map<String, TestCaseAggregator> aggregators) {
        this.aggregators = aggregators;
    }

    @SuppressWarnings("unchecked")
    public Consumer<Map<String, Object>> process(final TestRun testRun, final TestCase testCase) {
        return data -> aggregators.forEach((uid, aggregator) -> {
            final Object value = data.computeIfAbsent(uid, key -> aggregator.supplier(testRun).get());
            aggregator.aggregate(testRun, testCase).accept(value);
        });
    }
}
