package org.allurefw.report.xunit;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.XunitData;
import org.allurefw.report.XunitWidgetData;

import java.util.stream.Collectors;

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
                .toWidget("xunit", this::widget);
    }

    protected Object widget(XunitData identity) {
        return identity.getTestSuites().stream()
                .limit(10)
                .map(testSuite -> new XunitWidgetData()
                        .withUid(testSuite.getUid())
                        .withName(testSuite.getName()))
                .collect(Collectors.toList());
    }
}
