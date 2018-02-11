package io.qameta.allure;

import io.qameta.allure.metric.Metric;
import io.qameta.allure.trend.Trend;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultPluginRegistry implements PluginRegistry {

    private final Set<Metric> metrics = new CopyOnWriteArraySet<>();
    private final Set<Trend> trends = new CopyOnWriteArraySet<>();
    private final Set<Aggregator> aggregators = new CopyOnWriteArraySet<>();

    @Override
    public void addMetric(final Metric metric) {
        metrics.add(metric);
    }

    @Override
    public void addTrend(final Trend trend) {
        trends.add(trend);
    }

    @Override
    public void addAggregator(final Aggregator aggregator) {
        aggregators.add(aggregator);
    }

    public Set<Metric> getMetrics() {
        return Collections.unmodifiableSet(metrics);
    }

    public Set<Trend> getTrends() {
        return Collections.unmodifiableSet(trends);
    }

    public Set<Aggregator> getAggregators() {
        return aggregators;
    }
}
