package org.allurefw.report.core;

import org.allurefw.report.TestCaseAggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestRun;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessTestCaseStage {

    private final Map<String, TestCaseAggregator> aggregators;

    @Inject
    public ProcessTestCaseStage(Map<String, TestCaseAggregator> aggregators) {
        this.aggregators = aggregators;
    }

    public Consumer<Map<String, Object>> process(TestRun testRun, TestCase testCase) {
        return data -> aggregators.forEach((uid, aggregator) -> {
            Object value = data.computeIfAbsent(uid, key -> aggregator.supplier(testRun).get());
            //noinspection unchecked
            aggregator.aggregate(testRun, testCase).accept(value);
        });
    }
}
