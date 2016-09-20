package org.allurefw.report.total;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCaseResult;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
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
    public BinaryOperator<TotalData> combiner() {
        return (first, second) -> {
            first.getTime().merge(second.getTime());
            first.getStatistic().merge(second.getStatistic());
            return first;
        };
    }

    @Override
    public BiConsumer<TotalData, TestCaseResult> accumulator() {
        return (totalData, result) -> {
            totalData.updateStatistic(result);
            totalData.updateTime(result);
        };
    }
}
