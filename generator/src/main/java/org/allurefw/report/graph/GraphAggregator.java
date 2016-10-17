package org.allurefw.report.graph;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class GraphAggregator implements Aggregator<GraphData> {

    @Override
    public Supplier<GraphData> supplier(TestRun testRun) {
        return GraphData::new;
    }

    @Override
    public Consumer<GraphData> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return identity -> identity.getTestCases().add(result.toInfo());
    }
}
