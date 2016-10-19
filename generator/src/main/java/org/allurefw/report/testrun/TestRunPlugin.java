package org.allurefw.report.testrun;

import org.allurefw.report.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestRunPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(TestRunStatisticAggregator.class)
                .toWidget("testRuns");
    }
}
