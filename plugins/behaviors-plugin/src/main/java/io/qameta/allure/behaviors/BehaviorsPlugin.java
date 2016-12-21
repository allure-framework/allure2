package io.qameta.allure.behaviors;

import io.qameta.allure.AbstractPlugin;

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
