package io.qameta.allure.duration;

import io.qameta.allure.entity.GroupTime;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class DurationTrendItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long buildOrder;
    protected String reportUrl;
    protected String reportName;
    protected GroupTime time = new GroupTime();
}
