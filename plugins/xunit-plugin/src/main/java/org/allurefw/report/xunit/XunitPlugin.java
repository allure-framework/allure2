package org.allurefw.report.xunit;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.tree.TreeWidgetFinalizer;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
@Plugin(name = "xunit")
public class XunitPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(XunitAggregator.class)
                .toReportData("xunit.json")
                .toWidget("xunit", TreeWidgetFinalizer.class);
    }
}
