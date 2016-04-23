package org.allurefw.report.xunit;

import org.allurefw.report.Finalizer;
import org.allurefw.report.TestSuite;
import org.allurefw.report.XunitData;
import org.allurefw.report.XunitWidgetData;

import java.util.Comparator;
import java.util.stream.Collectors;

import static org.allurefw.report.entity.ExtraStatisticMethods.comparator;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 23.04.16
 */
public class XunitWidgetFinalizer implements Finalizer<XunitData> {

    @Override
    public Object finalize(XunitData identity) {
        return identity.getTestSuites().stream()
                .sorted(Comparator.comparing(TestSuite::getStatistic, comparator()).reversed())
                .limit(10)
                .map(testSuite -> new XunitWidgetData()
                        .withUid(testSuite.getUid())
                        .withName(testSuite.getName())
                        .withStatistic(testSuite.getStatistic()))
                .collect(Collectors.toList());
    }
}
