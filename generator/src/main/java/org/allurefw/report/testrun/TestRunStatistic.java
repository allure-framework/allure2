package org.allurefw.report.testrun;

import org.allurefw.report.entity.Statistic;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestRunStatistic {

    private String name;

    private Statistic statistic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public void setStatistic(Statistic statistic) {
        this.statistic = statistic;
    }
}
