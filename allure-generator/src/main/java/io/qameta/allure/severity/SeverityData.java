package io.qameta.allure.severity;

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
public class SeverityData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected String name;
    protected Time time;
    protected Status status;
    protected SeverityLevel severity;
}
