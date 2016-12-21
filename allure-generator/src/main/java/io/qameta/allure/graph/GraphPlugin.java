package io.qameta.allure.graph;

import io.qameta.allure.AbstractPlugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
public class GraphPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(GraphResultAggregator.class)
                .toReportData("graph.json");
    }
}
