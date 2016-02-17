package org.allurefw.report.graph;

import com.google.inject.Inject;
import org.allurefw.report.GraphData;
import org.allurefw.report.ReportDataProvider;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class GraphDataProvider implements ReportDataProvider {

    protected final GraphData data;

    @Inject
    protected GraphDataProvider(GraphData data) {
        this.data = data;
    }

    @Override
    public Object provide() {
        return data;
    }

    @Override
    public String getFileName() {
        return "graph.json";
    }
}
