package org.allurefw.report.summary;

import org.allurefw.report.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class SummaryPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(SummaryAggregator.class)
                .toWidget("summary");
    }
}
