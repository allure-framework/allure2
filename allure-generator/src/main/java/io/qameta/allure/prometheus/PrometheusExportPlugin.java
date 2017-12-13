package io.qameta.allure.prometheus;

import io.qameta.allure.CommonMetricAggregator;
import io.qameta.allure.category.CategoriesMetric;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.StatusMetric;
import io.qameta.allure.metric.TimeMetric;
import io.qameta.allure.retry.RetryMetric;

import java.util.Arrays;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class PrometheusExportPlugin extends CommonMetricAggregator {

    public PrometheusExportPlugin() {
        super("prometheusData.txt");
    }

    @Override
    public List<Metric> getMetrics() {
        final StatusMetric statusMetric = new StatusMetric((status, count) ->
                new PrometheusMetricLine("launch_status", status.value(), String.valueOf(count)));

        final TimeMetric timeMetric = new TimeMetric((key, time) ->
                new PrometheusMetricLine("launch_time", key, String.valueOf(time)));

        final CategoriesMetric categoriesMetric = new CategoriesMetric((category, count) ->
                new PrometheusMetricLine("launch_problems", category, String.valueOf(count)));

        final RetryMetric retryMetric = new RetryMetric((key, count) ->
                new PrometheusMetricLine("launch_retries", key, String.valueOf(count))
        );

        return Arrays.asList(
                statusMetric,
                timeMetric,
                categoriesMetric,
                retryMetric
        );
    }
}
