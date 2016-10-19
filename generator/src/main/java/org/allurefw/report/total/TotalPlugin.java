package org.allurefw.report.total;

import org.allurefw.report.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class TotalPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(TotalResultAggregator.class).toWidget("total");
    }
}
