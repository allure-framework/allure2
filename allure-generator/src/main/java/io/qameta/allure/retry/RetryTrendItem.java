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
