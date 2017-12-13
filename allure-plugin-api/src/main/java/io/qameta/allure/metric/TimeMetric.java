package io.qameta.allure.metric;

import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.TestResult;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author charlie (Dmitry Baev).
 */
public class TimeMetric implements Metric {

    private final GroupTime groupTime = new GroupTime();

    private final BiFunction<String, Long, MetricLine> lineFactory;

    public TimeMetric(final BiFunction<String, Long, MetricLine> lineFactory) {
        this.lineFactory = lineFactory;
    }

    @Override
    public void update(final TestResult testResult) {
        if (!testResult.isRetry()) {
            groupTime.update(testResult);
        }
    }

    @Override
    public List<MetricLine> getLines() {
        return Arrays.asList(
                lineFactory.apply("duration", zeroIfNull(groupTime.getDuration())),
                lineFactory.apply("min_duration", zeroIfNull(groupTime.getMinDuration())),
                lineFactory.apply("max_duration", zeroIfNull(groupTime.getMaxDuration())),
                lineFactory.apply("sum_duration", zeroIfNull(groupTime.getSumDuration()))
        );
    }

    private static long zeroIfNull(final Long value) {
        return Objects.isNull(value) ? 0 : value;
    }
}
