package org.allurefw.report.behaviors;

import org.allurefw.report.AbstractPlugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public class BehaviorsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(BehaviorsResultAggregator.class)
                .toReportData("behaviors.json");
    }
}
