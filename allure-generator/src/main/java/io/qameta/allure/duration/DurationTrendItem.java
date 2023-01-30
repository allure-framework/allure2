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
package io.qameta.allure.duration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Timeable;
import io.qameta.allure.trend.TrendItem;

/**
 * @author charlie (Dmitry Baev).
 */
public class DurationTrendItem extends TrendItem {

    private static final String DURATION_KEY = "duration";

    @JsonIgnore
    private final GroupTime time = new GroupTime();

    public void updateTime(final Timeable timeable) {
        time.update(timeable);
        if (time.getDuration() != null) {
            setMetric(DURATION_KEY, time.getDuration());
        } else {
            setMetric(DURATION_KEY, 0L);
        }
    }

}
