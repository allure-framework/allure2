package org.allurefw.report.graph;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Aggregator;
import org.allurefw.report.GraphData;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "graph")
public class GraphPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(new GraphData(), this::getAggregator);
    }

    protected Aggregator<GraphData> getAggregator() {
        return (identity, testCase) -> identity.getTestCases().add(testCase.toInfo());
    }
}
