package io.qameta.allure.xunit;

import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.tree.TreeWidgetFinalizer;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
public class XunitPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(XunitResultAggregator.class)
                .toReportData("xunit.json")
                .toWidget("xunit", TreeWidgetFinalizer.class);
    }
}
