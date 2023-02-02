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
package io.qameta.allure.metric;

import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class StatusMetric implements Metric {

    private final Statistic statistic = new Statistic();

    private final BiFunction<Status, Long, MetricLine> lineFactory;

    public StatusMetric(final BiFunction<Status, Long, MetricLine> lineFactory) {
        this.lineFactory = lineFactory;
    }

    @Override
    public void update(final TestResult testResult) {
        if (testResult.isRetry()) {
            return;
        }
        statistic.update(testResult);
    }

    @Override
    public List<MetricLine> getLines() {
        return Stream.of(Status.values())
                .map(status -> lineFactory.apply(status, statistic.get(status)))
                .collect(Collectors.toList());
    }
}
