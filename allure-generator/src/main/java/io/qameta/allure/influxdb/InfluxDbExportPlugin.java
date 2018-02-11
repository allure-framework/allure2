package io.qameta.allure.influxdb;

import io.qameta.allure.AbstractMetricAggregator;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.StatusMetric;
import io.qameta.allure.metric.TimeMetric;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Plugin that generates data for influx db.
 */
public class InfluxDbExportPlugin extends AbstractMetricAggregator {

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

        return Arrays.asList(
                statusMetric,
                timeMetric
        );
    }


}
