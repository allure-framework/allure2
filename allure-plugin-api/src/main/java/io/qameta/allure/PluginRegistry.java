package io.qameta.allure;

import io.qameta.allure.metric.Metric;
import io.qameta.allure.trend.Trend;

/**
 * @author charlie (Dmitry Baev).
 */
public interface PluginRegistry {

    void addMetric(Metric metric);

    void addTrend(Trend trend);

    void addAggregator(Aggregator aggregator);

}
