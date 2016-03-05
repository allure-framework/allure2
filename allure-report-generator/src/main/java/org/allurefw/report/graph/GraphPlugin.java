package org.allurefw.report.graph;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.GraphData;
import org.allurefw.report.Plugin;
import org.allurefw.report.entity.TestCase;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "graph")
public class GraphPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        GraphData graphData = new GraphData();

        aggregator(graphData, this::aggregate);
        reportData(graphData);
    }

    protected void aggregate(GraphData identity, TestCase testCase) {
        identity.getTestCases().add(testCase.toInfo());
    }
}
