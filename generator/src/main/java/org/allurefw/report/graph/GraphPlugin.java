package org.allurefw.report.graph;

import org.allurefw.report.AbstractPlugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
public class GraphPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(GraphResultAggregator.class)
                .toReportData("graph.json");
    }
}
