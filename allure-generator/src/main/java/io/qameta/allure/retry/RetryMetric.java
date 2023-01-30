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
package io.qameta.allure.retry;

import io.qameta.allure.entity.TestResult;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.MetricLine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

/**
 * @author charlie (Dmitry Baev).
 */
public class RetryMetric implements Metric {

    private final AtomicLong retriesCount = new AtomicLong();
    private final AtomicLong runCount = new AtomicLong();

    private final BiFunction<String, Long, MetricLine> lineFactory;

    public RetryMetric(final BiFunction<String, Long, MetricLine> lineFactory) {
        this.lineFactory = lineFactory;
    }

    @Override
    public void update(final TestResult testResult) {
        if (testResult.isRetry()) {
            retriesCount.incrementAndGet();
        } else {
            runCount.incrementAndGet();
        }
    }

    @Override
    public List<MetricLine> getLines() {
        return Arrays.asList(
                lineFactory.apply("retries", retriesCount.longValue()),
                lineFactory.apply("run", runCount.longValue())
        );
    }
}
