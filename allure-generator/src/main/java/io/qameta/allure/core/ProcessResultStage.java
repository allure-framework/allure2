package io.qameta.allure.core;

import io.qameta.allure.Processor;
import io.qameta.allure.ResultAggregator;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessResultStage {

    @Inject
    protected Map<String, Processor> processors;

    @Inject
    protected Map<String, ResultAggregator> aggregators;

    public Consumer<Map<String, Object>> process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return data -> {
            processors.forEach((uid, processor) -> processor.process(testRun, testCase, result));
            aggregators.forEach((uid, aggregator) -> {
                Object value = data.computeIfAbsent(uid, key -> aggregator.supplier(testRun, testCase).get());
                //noinspection unchecked
                aggregator.aggregate(testRun, testCase, result).accept(value);
            });
        };
    }

}
