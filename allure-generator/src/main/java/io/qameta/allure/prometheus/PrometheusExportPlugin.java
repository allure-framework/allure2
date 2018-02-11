package io.qameta.allure.prometheus;

import io.qameta.allure.AbstractMetricAggregator;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.StatusMetric;
import io.qameta.allure.metric.TimeMetric;

import java.util.Arrays;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class PrometheusExportPlugin extends AbstractMetricAggregator {

    public PrometheusExportPlugin() {
        super("prometheusData.txt");
    }

    @Override
    public List<Metric> getMetrics() {
        final StatusMetric statusMetric = new StatusMetric((status, count) ->
                new PrometheusMetricLine("launch_status", status.value(), String.valueOf(count)));

        final TimeMetric timeMetric = new TimeMetric((key, time) ->
                new PrometheusMetricLine("launch_time", key, String.valueOf(time)));

        return Arrays.asList(
                statusMetric,
                timeMetric
        );
    }
}
