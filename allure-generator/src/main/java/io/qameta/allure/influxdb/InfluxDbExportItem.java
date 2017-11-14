package io.qameta.allure.influxdb;

import io.qameta.allure.category.Category;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Timeable;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Represent information for influx db export.
 */
@Data
@Accessors(chain = true)
public class InfluxDbExportItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String LAUNCH_STATUS = "launch_status";
    private static final String TOTAL = "total";

    private static final String LAUNCH_RETRIES = "launch_retries";
    private static final String RUN_KEY = "run";
    private static final String RETRY_KEY = "retry";

    private static final String LAUNCH_TIME = "launch_time";
    private static final String DURATION_KEY = "duration";
    private static final String MIN_DURATION_KEY = "minDuration";
    private static final String MAX_DURATION_KEY = "maxDuration";
    private static final String SUM_DURATION_KEY = "sumDuration";

    private static final String LAUNCH_PROBLEMS = "launch_problems";

    private final GroupTime time = new GroupTime();

    private static final List<String> METRICS_NAMES = Arrays.asList(
            LAUNCH_STATUS, LAUNCH_RETRIES, LAUNCH_TIME, LAUNCH_PROBLEMS
    );

    private final Map<String, Map<String, Long>> metrics = new HashMap<>();

    protected InfluxDbExportItem() {
        METRICS_NAMES.forEach(
            name -> this.metrics.put(name, new HashMap<>())
        );
    }

    protected void updateMetrics(final TestResult result) {
        updateStatus(result);
        updateRetries(result);
        updateTime(result);
        updateCategories(result);
    }

    private void updateStatus(final TestResult result) {
        if (result.isRetry()) {
            return;
        }
        this.increaseMetric(LAUNCH_STATUS, result.getStatus().value());
        this.increaseMetric(LAUNCH_STATUS, TOTAL);
    }

    private void updateCategories(final TestResult result) {
        result.<List<Category>>getExtraBlock("categories", new ArrayList<>()).stream()
                .map(Category::getName)
                .forEach(categoryName -> this.increaseMetric(LAUNCH_PROBLEMS, categoryName));
    }

    private void updateRetries(final TestResult result) {
        if (result.isRetry()) {
            this.increaseMetric(LAUNCH_RETRIES, RETRY_KEY);
        } else {
            this.increaseMetric(LAUNCH_RETRIES, RUN_KEY);
        }
    }

    private void updateTime(final Timeable timeable) {
        time.update(timeable);
        setMetric(LAUNCH_TIME, DURATION_KEY, this.time.getDuration());
        setMetric(LAUNCH_TIME, MIN_DURATION_KEY, this.time.getMinDuration());
        setMetric(LAUNCH_TIME, MAX_DURATION_KEY, this.time.getMaxDuration());
        setMetric(LAUNCH_TIME, SUM_DURATION_KEY, this.time.getSumDuration());
    }

    private void setMetric(final String name, final String key, final Long value) {
        this.metrics.get(name).put(key, value);
    }

    private void increaseMetric(final String name, final String metric) {
        long current = Optional.ofNullable(this.metrics.get(name).get(metric)).orElse(0L);
        this.metrics.get(name).put(metric, current + 1);
    }

    public String export() {
        final long timestamp = TimeUnit.SECONDS.toNanos(Instant.now().getEpochSecond());
        final StringBuilder builder = new StringBuilder();
        METRICS_NAMES.forEach(name -> addMetricData(builder, name, timestamp));
        return builder.toString();
    }

    private void addMetricData(final StringBuilder builder,
                               final String name,
                               final long timestamp) {
        Set<String> keySet = this.metrics.get(name).keySet();
        keySet.forEach(
            key -> builder.append(
                    metricValue(name, key, this.metrics.get(name).get(key), timestamp)
            )
        );
    }

    private String metricValue(final String name, final String key, final long value, final long timestamp) {
        return format("%s %s=%s %s%s",
                name,
                key.replaceAll("\\s", "\\\\ "),
                value,
                timestamp,
                System.lineSeparator()
        );
    }

}
