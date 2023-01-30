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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Anton Tsyganov (jenkl)
 */
class PrometheusMetricLineTest {
    private static final String METRIC_NAME = "launch";
    private static final String METRIC_KEY = "status passed";
    private static final String METRIC_VALUE = "300";

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("evn=\"test\",suite=\"regression\"", "launch_status_passed{evn=\"test\",suite=\"regression\"} 300",
                        "with labels"),
                Arguments.of(null, "launch_status_passed 300", "without labels")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "data")
    void shouldReturnMetric(final String labels, final String expectedMetric) {
        PrometheusMetricLine prometheusMetric = new PrometheusMetricLine(METRIC_NAME, METRIC_KEY, METRIC_VALUE, labels);
        assertThat(prometheusMetric.asString()).isEqualTo(expectedMetric);
    }
}
