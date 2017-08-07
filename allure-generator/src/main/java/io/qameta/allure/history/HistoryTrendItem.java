package io.qameta.allure.history;

import io.qameta.allure.entity.Statistic;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class HistoryTrendItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected Statistic statistic;
    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long buildOrder;
    @XmlElement(required = true)
    protected String reportUrl;
    @XmlElement(required = true)
    protected String reportName;
}
