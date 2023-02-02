/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

    private static final String ALLURE_PROMETHEUS_LABELS = "allure.prometheus.labels";

    public PrometheusExportPlugin() {
        super("prometheusData.txt");
    }

    @Override
    public List<Metric> getMetrics() {
        final String labels = getPrometheusLabels();

        final StatusMetric statusMetric = new StatusMetric((status, count) ->
                new PrometheusMetricLine("launch_status", status.value(), String.valueOf(count), labels));

        final TimeMetric timeMetric = new TimeMetric((key, time) ->
                new PrometheusMetricLine("launch_time", key, String.valueOf(time), labels));

        final CategoriesMetric categoriesMetric = new CategoriesMetric((category, count) ->
                new PrometheusMetricLine("launch_problems", category, String.valueOf(count), labels));

        final RetryMetric retryMetric = new RetryMetric((key, count) ->
                new PrometheusMetricLine("launch_retries", key, String.valueOf(count), labels)
        );

        return Arrays.asList(
                statusMetric,
                timeMetric,
                categoriesMetric,
                retryMetric
        );
    }

    public static String getPrometheusLabels() {
        if (System.getProperties().getProperty(ALLURE_PROMETHEUS_LABELS) != null) {
            return System.getProperties().getProperty(ALLURE_PROMETHEUS_LABELS);
        }
        return System.getenv(ALLURE_PROMETHEUS_LABELS);
    }
}
