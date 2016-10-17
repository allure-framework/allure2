package org.allurefw.report.history;

import org.allurefw.report.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        processor(HistoryProcessor.class);
        aggregator(HistoryAggregator.class).toReportData("history.json");
    }
}
