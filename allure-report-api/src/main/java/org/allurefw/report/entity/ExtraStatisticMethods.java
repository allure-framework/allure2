package org.allurefw.report.entity;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 23.04.16
 */
public interface ExtraStatisticMethods {

    long getFailed();

    void setFailed(Long value);

    long getBroken();

    void setBroken(Long value);

    long getPassed();

    void setPassed(Long value);

    long getPending();

    void setPending(Long value);

    long getCanceled();

    void setCanceled(Long value);

    @XmlElement
    default long getTotal() {
        return getFailed() + getBroken() + getPassed() + getCanceled() + getPending();
    }

    default void update(Status status) {
        if (status == null) {
            return;
        }
        switch (status) {
            case FAILED:
                setFailed(getFailed() + 1);
                break;
            case BROKEN:
                setBroken(getBroken() + 1);
                break;
            case CANCELED:
                setCanceled(getCanceled() + 1);
                break;
            case PASSED:
                setPassed(getPassed() + 1);
                break;
            case PENDING:
                setPending(getPending() + 1);
                break;
        }
    }

    default void merge(Statistic other) {
        if (other == null) {
            return;
        }
        setFailed(getFailed() + other.getFailed());
        setBroken(getBroken() + other.getBroken());
        setPassed(getPassed() + other.getPassed());
        setCanceled(getCanceled() + other.getCanceled());
        setPending(getPending() + other.getPending());
    }
}
