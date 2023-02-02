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

import io.qameta.allure.metric.MetricLine;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class PrometheusMetricLine implements MetricLine {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String key;
    private final String value;
    private final String labels;

    @Override
    public String asString() {
        return String.format("%s_%s%s %s",
                getName(),
                normalize(getKey()),
                normalizeLabels(getLabels()),
                getValue()
        );
    }

    public static String normalize(final String string) {
        return string.toLowerCase().replaceAll("\\s+", "_");
    }

    private String normalizeLabels(final String labels) {
        return labels == null ? "" : "{" + labels + "}";
    }
}
