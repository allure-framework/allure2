package io.qameta.allure.graph;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Time;
import io.qameta.allure.severity.SeverityLevel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class GraphData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected String name;
    protected Time time;
    protected Status status;
    protected SeverityLevel severity;
}
