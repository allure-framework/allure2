package org.allurefw.report.environment;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCase;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class NoopAggregator implements Aggregator<Object> {

    @Override
    public Supplier<Object> supplier() {
        return Object::new;
    }

    @Override
    public BinaryOperator<Object> combiner() {
        return (o, o2) -> o;
    }

    @Override
    public BiConsumer<Object, TestCase> accumulator() {
        return (o, tcase) -> {
        };
    }
}
