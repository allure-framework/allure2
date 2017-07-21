package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class ExecutorInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected String type;
    protected String url;
    protected Long buildOrder;
    protected String buildName;
    protected String buildUrl;
    protected String reportName;
    protected String reportUrl;

}
