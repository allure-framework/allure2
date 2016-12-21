package io.qameta.allure.testrun;

import io.qameta.allure.AbstractPlugin;

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
