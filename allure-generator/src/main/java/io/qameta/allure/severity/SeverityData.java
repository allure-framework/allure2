package io.qameta.allure.severity;

import io.qameta.allure.entity.TestStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class SeverityData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long id;
    protected String name;
    protected TestStatus status;
    protected SeverityLevel severity;

    protected Long start;
    protected Long stop;
    protected Long duration;

}
