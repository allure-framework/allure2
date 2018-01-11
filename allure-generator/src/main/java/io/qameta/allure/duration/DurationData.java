package io.qameta.allure.duration;

import io.qameta.allure.entity.TestStatus;
import io.qameta.allure.severity.SeverityLevel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class DurationData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long id;
    protected String name;

    protected Long start;
    protected Long stop;
    protected Long duration;

    protected TestStatus status;
    protected SeverityLevel severity;
}
