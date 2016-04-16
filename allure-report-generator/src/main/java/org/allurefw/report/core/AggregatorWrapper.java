package org.allurefw.report.core;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCase;

import java.util.function.Function;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 16.04.16
 */
public class AggregatorWrapper<T> {

    private Aggregator<T> aggregator;

    private T aggregationResult;

    public AggregatorWrapper(Aggregator<T> aggregator) {
        this.aggregator = aggregator;
        this.aggregationResult = aggregator.supplier().get();
    }

    public void accept(TestCase testCase) {
        aggregator.accumulator().accept(aggregationResult, testCase);
    }

    public <R> R get(Function<T, R> function) {
        return function.apply(aggregationResult);
    }

}
