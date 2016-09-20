package org.allurefw.report.graph;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCaseResult;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class GraphAggregator implements Aggregator<GraphData> {

    @Override
    public Supplier<GraphData> supplier() {
        return GraphData::new;
    }

    @Override
    public BinaryOperator<GraphData> combiner() {
        return (left, right) -> left.withTestCases(right.getTestCases());
    }

    @Override
    public BiConsumer<GraphData, TestCaseResult> accumulator() {
        return (identity, testCase) -> identity.getTestCases().add(testCase.toInfo());
    }
}
