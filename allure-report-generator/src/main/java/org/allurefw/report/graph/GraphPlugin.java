package org.allurefw.report.graph;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "graph")
public class GraphPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        use(GraphAggregator.class)
                .asAggregator()
                .toReportData("graph.json");
    }
}
