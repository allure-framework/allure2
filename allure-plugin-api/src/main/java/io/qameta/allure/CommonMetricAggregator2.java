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
package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.MetricLine;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public abstract class CommonMetricAggregator2 implements Aggregator2 {

    private final String location;

    private final String fileName;

    protected CommonMetricAggregator2(final String fileName) {
        this(Constants.EXPORT_DIR, fileName);
    }

    protected CommonMetricAggregator2(final String location, final String fileName) {
        this.location = location;
        this.fileName = fileName;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {

        storage.addDataBinary(
                Constants.path(location, fileName),
                getData(launchesResults).getBytes(StandardCharsets.UTF_8)
        );
    }

    public abstract List<Metric> getMetrics();

    @SuppressWarnings("MultipleStringLiterals")
    protected String getData(final List<LaunchResults> launchesResults) {
        final List<Metric> metrics = getMetrics();
        final List<TestResult> results = launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (TestResult result : results) {
            for (Metric metric : metrics) {
                metric.update(result);
            }
        }

        return metrics.stream()
                .map(Metric::getLines)
                .flatMap(Collection::stream)
                .map(MetricLine::asString)
                .collect(Collectors.joining("\n", "", "\n"));
    }

}
