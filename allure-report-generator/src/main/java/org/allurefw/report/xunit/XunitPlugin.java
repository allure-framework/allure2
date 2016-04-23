package org.allurefw.report.xunit;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "xunit")
public class XunitPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(XunitAggregator.class)
                .toReportData("xunit.json")
                .toWidget("xunit", XunitWidgetFinalizer.class);
    }
}
