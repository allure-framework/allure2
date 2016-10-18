package org.allurefw.report.timeline;

import org.allurefw.report.AbstractPlugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
public class TimelinePlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(TimelineResultAggregator.class)
                .toReportData("timeline.json");
    }

}
