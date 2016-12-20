package org.allurefw.report.entity;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithStatistic {

    Statistic getStatistic();

    void setStatistic(Statistic statistic);

    default void updateStatistic(Statistic other) {
        getStatistic().setPassed(other.getPassed() + getStatistic().getPassed());
        getStatistic().setBroken(other.getBroken() + getStatistic().getBroken());
        getStatistic().setCanceled(other.getCanceled() + getStatistic().getCanceled());
        getStatistic().setFailed(other.getFailed() + getStatistic().getFailed());
        getStatistic().setPending(other.getPending() + getStatistic().getPending());
    }

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
