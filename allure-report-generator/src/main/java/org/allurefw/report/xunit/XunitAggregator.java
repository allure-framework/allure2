package org.allurefw.report.xunit;

import org.allurefw.report.Aggregator;
import org.allurefw.report.DataCollector;
import org.allurefw.report.TestSuite;
import org.allurefw.report.XunitData;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCase;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 05.03.16
 */
public class XunitAggregator implements Aggregator<XunitData> {

    @Override
    public Supplier<XunitData> supplier() {
        return XunitData::new;
    }

    @Override
    public BiConsumer<XunitData, TestCase> accumulator() {
        return (xunit, testCase) -> {
            String suiteName = testCase.findOne(LabelName.SUITE)
                    .orElse("Default suite");

            TestSuite testSuite = xunit.getTestSuites().stream()
                    .filter(item -> suiteName.equals(item.getName()))
                    .findAny()
                    .orElseGet(() -> {
                        TestSuite newOne = new TestSuite().withName(suiteName).withUid(generateUid());
                        xunit.getTestSuites().add(newOne);
                        return newOne;
                    });

            testSuite.getTestCases().add(testCase.toInfo());
        };
    }

    @Override
    public BinaryOperator<XunitData> combiner() {
        return (left, right) -> {
            left.getTestSuites().addAll(right.getTestSuites());
            return left;
        };
    }
}
