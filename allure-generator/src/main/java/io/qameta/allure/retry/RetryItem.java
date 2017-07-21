package io.qameta.allure.retry;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Time;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class RetryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected Status status;
    protected String statusDetails;
    protected Time time;

}
