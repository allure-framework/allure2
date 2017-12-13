package io.qameta.allure.summary;

import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Statistic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class SummaryData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String reportName;
    protected List<String> testRuns = new ArrayList<>();
    protected Statistic statistic = new Statistic();
    protected GroupTime time = new GroupTime();

}
