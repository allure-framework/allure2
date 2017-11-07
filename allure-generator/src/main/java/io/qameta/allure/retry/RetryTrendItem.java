package io.qameta.allure.retry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Represent information about retries.
 */
@Data
@Accessors(chain = true)
public class RetryTrendItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long buildOrder;
    protected String reportUrl;
    protected String reportName;
    protected int retryNumber;

    protected void updateNumber() {
        this.retryNumber ++;
    }
}
