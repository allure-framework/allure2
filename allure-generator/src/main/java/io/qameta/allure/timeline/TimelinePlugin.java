package io.qameta.allure.timeline;

import io.qameta.allure.AbstractPlugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
public class TimelinePlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(TimelineResultAggregator.class)
                .toReportData("timeline.json");
    }

}
