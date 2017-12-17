package io.qameta.allure.history;

import io.qameta.allure.entity.TestStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class HistoryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected String reportUrl;
    protected TestStatus status;
    protected String statusDetails;

    protected Long start;
    protected Long stop;
    protected Long duration;

}
