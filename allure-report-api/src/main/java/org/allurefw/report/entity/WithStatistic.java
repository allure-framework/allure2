package org.allurefw.report.entity;

import org.allurefw.Status;
import org.allurefw.report.Statistic;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithStatistic {

    Statistic getStatistic();

    default void update(WithStatus withStatus) {
        Status status = withStatus.getStatus();
        Statistic stat = getStatistic();
        stat.setTotal(stat.getTotal() + 1);
        switch (status) {
            case FAILED:
                stat.setFailed(stat.getFailed() + 1);
                break;
            case BROKEN:
                stat.setBroken(stat.getBroken() + 1);
                break;
            case CANCELED:
                stat.setCanceled(stat.getCanceled() + 1);
                break;
            case PASSED:
                stat.setPassed(stat.getPassed() + 1);
                break;
            case PENDING:
                stat.setPending(stat.getPending() + 1);
                break;
        }
    }
}
