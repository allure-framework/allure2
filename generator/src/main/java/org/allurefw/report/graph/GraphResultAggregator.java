package org.allurefw.report.graph;

import org.allurefw.report.ResultAggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class GraphResultAggregator implements ResultAggregator<List<GraphData>> {

    @Override
    public Supplier<List<GraphData>> supplier(TestRun testRun, TestCase testCase) {
        return ArrayList::new;
    }

    @Override
    public Consumer<List<GraphData>> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return data -> data.add(new GraphData()
                .withUid(result.getUid())
                .withName(result.getName())
                .withStatus(result.getStatus())
                .withTime(result.getTime())
                .withSeverity(result.getExtraBlock("severity"))
        );
    }
}
