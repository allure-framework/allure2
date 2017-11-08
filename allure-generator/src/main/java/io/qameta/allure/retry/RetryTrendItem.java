package io.qameta.allure.retry;

import io.qameta.allure.trend.TrendItem;

/**
 * Represent information about retries.
 */
public class RetryTrendItem extends TrendItem {

    private static final String RETRY_KEY = "retry";

    public RetryTrendItem() {
        this.setMetric(RETRY_KEY, 0L);
    }

    public void increaseRetryCount() {
        this.increaseMetric(RETRY_KEY);
    }

}
