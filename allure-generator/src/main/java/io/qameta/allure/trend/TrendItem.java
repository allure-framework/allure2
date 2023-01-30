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
package io.qameta.allure.trend;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Trend item data.
 *
 * @author eroshenkoam
 */
@Getter
@Setter
@Accessors(chain = true)
public class TrendItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long buildOrder;

    protected String reportUrl;

    protected String reportName;

    protected Map<String, Long> data = new HashMap<>();

    protected void increaseMetric(final String metric) {
        final long current = Optional.ofNullable(data.get(metric)).orElse(0L);
        data.put(metric, current + 1);
    }

    protected void setMetric(final String metric, final long value) {
        this.data.put(metric, value);
    }

}
