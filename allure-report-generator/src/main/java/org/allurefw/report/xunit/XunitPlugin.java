package org.allurefw.report.xunit;

import org.allurefw.LabelName;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.TestSuite;
import org.allurefw.report.XunitData;
import org.allurefw.report.XunitWidgetData;
import org.allurefw.report.entity.TestCase;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "xunit")
public class XunitPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        XunitData xunitData = new XunitData();

        aggregator(xunitData, this::aggregate);
        widgetData(xunitData, this::getWidgetData);
        reportData(xunitData);
    }

    protected void aggregate(XunitData identity, TestCase testCase) {
        String suiteName = testCase.findOne(LabelName.SUITE)
                .orElse("Default suite");

        TestSuite testSuite = identity.getTestSuites().stream()
                .filter(item -> suiteName.equals(item.getName()))
                .findAny()
                .orElseGet(() -> {
                    TestSuite newOne = new TestSuite().withName(suiteName).withUid(generateUid());
                    identity.getTestSuites().add(newOne);
                    return newOne;
                });

        testSuite.getTestCases().add(testCase.toInfo());
    }

    protected ArrayList<XunitWidgetData> getWidgetData(XunitData identity) {
        return new ArrayList<>(identity.getTestSuites().stream()
                .limit(10)
                .map(testSuite -> new XunitWidgetData()
                        .withUid(testSuite.getUid())
                        .withName(testSuite.getName()))
                .collect(Collectors.toList()));
    }

}
