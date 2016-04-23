package org.allurefw.report.entity;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithStatistic {

    Statistic getStatistic();

    void setStatistic(Statistic statistic);

    default void updateStatistic(WithStatus withStatus) {
        if (withStatus == null) {
            return;
        }
        if (getStatistic() == null) {
            setStatistic(new Statistic());
        }
        getStatistic().update(withStatus.getStatus());
    }
}
