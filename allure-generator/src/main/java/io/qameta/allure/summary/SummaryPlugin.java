package io.qameta.allure.summary;

import io.qameta.allure.AbstractPlugin;

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
