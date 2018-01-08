package io.qameta.allure.metric;

import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class StatusMetric implements Metric {

    private final Statistic statistic = new Statistic();

    private final BiFunction<Status, Long, MetricLine> lineFactory;

    public StatusMetric(final BiFunction<Status, Long, MetricLine> lineFactory) {
        this.lineFactory = lineFactory;
    }

    @Override
    public void update(final TestResult testResult) {
        if (testResult.isRetry()) {
            return;
        }
        statistic.update(testResult);
    }

    @Override
    public List<MetricLine> getLines() {
        return Stream.of(Status.values())
                .map(status -> lineFactory.apply(status, statistic.get(status)))
                .collect(Collectors.toList());
    }
}
