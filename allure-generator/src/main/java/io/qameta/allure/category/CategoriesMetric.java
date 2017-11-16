package io.qameta.allure.category;

import io.qameta.allure.entity.TestResult;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.MetricLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class CategoriesMetric implements Metric {

    private final Map<String, AtomicLong> values = new HashMap<>();

    private final BiFunction<String, Long, MetricLine> lineFactory;

    public CategoriesMetric(final BiFunction<String, Long, MetricLine> lineFactory) {
        this.lineFactory = lineFactory;
    }

    @Override
    public void update(final TestResult testResult) {
        if (testResult.isRetry()) {
            return;
        }
        testResult.<List<Category>>getExtraBlock("categories", new ArrayList<>()).stream()
                .map(Category::getName)
                .forEach(categoryName -> values.computeIfAbsent(categoryName, s -> new AtomicLong()).incrementAndGet());
    }

    @Override
    public List<MetricLine> getLines() {
        return values.entrySet().stream()
                .map(entry -> lineFactory.apply(entry.getKey(), entry.getValue().longValue()))
                .collect(Collectors.toList());
    }
}
