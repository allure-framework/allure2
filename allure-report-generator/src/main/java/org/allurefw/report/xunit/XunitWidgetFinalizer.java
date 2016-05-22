package org.allurefw.report.xunit;

import org.allurefw.report.Finalizer;
import org.allurefw.report.TestSuite;
import org.allurefw.report.XunitData;
import org.allurefw.report.XunitWidgetData;
import org.allurefw.report.XunitWidgetItem;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.allurefw.report.entity.ExtraStatisticMethods.comparator;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 23.04.16
 */
public class XunitWidgetFinalizer implements Finalizer<XunitData> {

    @Override
    public Object finalize(XunitData identity) {
        List<XunitWidgetItem> items = identity.getTestSuites().stream()
                .sorted(Comparator.comparing(TestSuite::getStatistic, comparator()).reversed())
                .limit(10)
                .map(testSuite -> new XunitWidgetItem()
                        .withUid(testSuite.getUid())
                        .withName(testSuite.getName())
                        .withStatistic(testSuite.getStatistic()))
                .collect(Collectors.toList());
        return new XunitWidgetData()
                .withItems(items)
                .withTotal(identity.getStatistic().getTotal());
    }
}
