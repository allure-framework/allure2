package io.qameta.allure.metric;

import io.qameta.allure.entity.TestResult;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Metric {

    void update(TestResult testResult);

    List<MetricLine> getLines();

}
