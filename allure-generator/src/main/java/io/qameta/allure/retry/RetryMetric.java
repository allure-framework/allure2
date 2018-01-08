package io.qameta.allure.retry;

import io.qameta.allure.entity.TestResult;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.MetricLine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

/**
 * @author charlie (Dmitry Baev).
 */
public class RetryMetric implements Metric {

    private final AtomicLong retriesCount = new AtomicLong();
    private final AtomicLong runCount = new AtomicLong();

    private final BiFunction<String, Long, MetricLine> lineFactory;

    public RetryMetric(final BiFunction<String, Long, MetricLine> lineFactory) {
        this.lineFactory = lineFactory;
    }

    @Override
    public void update(final TestResult testResult) {
        if (testResult.isRetry()) {
            retriesCount.incrementAndGet();
        } else {
            runCount.incrementAndGet();
        }
    }

    @Override
    public List<MetricLine> getLines() {
        return Arrays.asList(
                lineFactory.apply("retries", retriesCount.longValue()),
                lineFactory.apply("run", runCount.longValue())
        );
    }
}
