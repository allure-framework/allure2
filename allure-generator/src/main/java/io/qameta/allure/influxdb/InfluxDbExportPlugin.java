package io.qameta.allure.influxdb;

import io.qameta.allure.CommonMetricAggregator;
import io.qameta.allure.category.CategoriesMetric;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.StatusMetric;
import io.qameta.allure.metric.TimeMetric;
import io.qameta.allure.retry.RetryMetric;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Plugin that generates data for influx db.
 */
public class InfluxDbExportPlugin extends CommonMetricAggregator {

    public InfluxDbExportPlugin() {
        super("influxDbData.txt");
    }

    @Override
    public List<Metric> getMetrics() {
        final long timestamp = TimeUnit.SECONDS.toNanos(Instant.now().getEpochSecond());
        final StatusMetric statusMetric = new StatusMetric((status, count) ->
                new InfluxDbMetricLine("launch_status", status.value(), String.valueOf(count), timestamp));

        final TimeMetric timeMetric = new TimeMetric((key, time) ->
                new InfluxDbMetricLine("launch_time", key, String.valueOf(time), timestamp));

        final CategoriesMetric categoriesMetric = new CategoriesMetric((category, count) ->
                new InfluxDbMetricLine("launch_problems", category, String.valueOf(count), timestamp));

        final RetryMetric retryMetric = new RetryMetric((key, count) ->
                new InfluxDbMetricLine("launch_retries", key, String.valueOf(count), timestamp)
        );

        return Arrays.asList(
                statusMetric,
                timeMetric,
                categoriesMetric,
                retryMetric
        );
    }


}
