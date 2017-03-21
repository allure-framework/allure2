package io.qameta.allure.graph;

import io.qameta.allure.ResultAggregator;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

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
    public Supplier<List<GraphData>> supplier(final TestRun testRun, final TestCase testCase) {
        return ArrayList::new;
    }

    @Override
    public Consumer<List<GraphData>> aggregate(final TestRun testRun,
                                               final TestCase testCase,
                                               final TestCaseResult result) {
        return data -> data.add(new GraphData()
                .withUid(result.getUid())
                .withName(result.getName())
                .withStatus(result.getStatus())
                .withTime(result.getTime())
                .withSeverity(result.getExtraBlock("severity"))
        );
    }
}
