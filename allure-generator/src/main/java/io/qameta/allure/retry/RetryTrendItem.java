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
import io.qameta.allure.trend.TrendItem;

/**
 * Represent information about retries.
 */
public class RetryTrendItem extends TrendItem {

    private static final String RUN_KEY = "run";

    private static final String RETRY_KEY = "retry";

    public RetryTrendItem() {
        this.setMetric(RETRY_KEY, 0L);
        this.setMetric(RUN_KEY, 0L);
    }

    public void update(final TestResult result) {
        if (result.isRetry()) {
            this.increaseMetric(RETRY_KEY);
        } else {
            this.increaseMetric(RUN_KEY);
        }
    }

}
