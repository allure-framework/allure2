package org.allurefw.report.xunit;

import org.allurefw.report.Aggregator;
import org.allurefw.report.TestSuite;
import org.allurefw.report.XunitData;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.Time;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 05.03.16
 */
public class XunitAggregator implements Aggregator<XunitData> {

    @Override
    public Supplier<XunitData> supplier() {
        return () -> new XunitData()
                .withTime(
                        new Time()
                                .withStart(Long.MAX_VALUE)
                                .withStop(Long.MIN_VALUE)
                );
    }

    @Override
    public BinaryOperator<XunitData> combiner() {
        return (left, right) -> left.withTestSuites(right.getTestSuites());
    }

    @Override
    public BiConsumer<XunitData, TestCase> accumulator() {
        return (xunit, testCase) -> {
            xunit.updateStatistic(testCase);
            xunit.withTime(time().apply(xunit.getTime(), testCase.getTime()));

            String suiteName = testCase.findOne(LabelName.SUITE)
                    .orElse("Default suite");

            TestSuite testSuite = xunit.getTestSuites().stream()
                    .filter(item -> suiteName.equals(item.getName()))
                    .findAny()
                    .orElseGet(() -> {
                        TestSuite newOne = new TestSuite()
                                .withName(suiteName)
                                .withUid(generateUid())
                                .withTime(new Time()
                                        .withStart(Long.MAX_VALUE)
                                        .withStop(Long.MIN_VALUE)
                                );
                        xunit.getTestSuites().add(newOne);
                        return newOne;
                    });

            testSuite.updateStatistic(testCase);
            testSuite.withTime(time().apply(testSuite.getTime(), testCase.getTime()));
            testSuite.getTestCases().add(testCase.toInfo());
        };
    }

    private BinaryOperator<Time> time() {
        return (first, second) -> {
            Time result = new Time()
                    .withStart(LongStream.of(first.getStart(), second.getStart()).min().orElse(Long.MAX_VALUE))
                    .withStop(LongStream.of(first.getStop(), second.getStop()).max().orElse(Long.MIN_VALUE));
            return result.withDuration(result.getStop() - result.getStart());
        };
    }
}
