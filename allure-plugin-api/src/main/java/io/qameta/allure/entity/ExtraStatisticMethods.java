package io.qameta.allure.entity;

import javax.xml.bind.annotation.XmlElement;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 23.04.16
 */
public interface ExtraStatisticMethods {

    long getFailed();

    void setFailed(long value);

    long getBroken();

    void setBroken(long value);

    long getPassed();

    void setPassed(long value);

    long getSkipped();

    void setSkipped(long value);

    long getUnknown();

    void setUnknown(long value);

    @XmlElement
    default long getTotal() {
        return getFailed() + getBroken() + getPassed() + getSkipped() + getUnknown();
    }

    /**
     * To ignore total property during deserialization. Should not be used manually.
     *
     * @deprecated Do not use it manually.
     */
    @Deprecated
    @XmlElement
    default void setTotal(final long total) {
        //do nothing
    }

    default void update(final WithStatus withStatus) {
        if (Objects.isNull(withStatus)) {
            return;
        }
        update(withStatus.getStatus());
    }

    default void update(final Status status) {
        if (Objects.isNull(status)) {
            return;
        }
        switch (status) {
            case FAILED:
                setFailed(getFailed() + 1);
                break;
            case BROKEN:
                setBroken(getBroken() + 1);
                break;
            case PASSED:
                setPassed(getPassed() + 1);
                break;
            case SKIPPED:
                setSkipped(getSkipped() + 1);
                break;
            default:
                setUnknown(getUnknown() + 1);
                break;
        }
    }

    default void merge(final Statistic other) {
        if (Objects.isNull(other)) {
            return;
        }
        setFailed(getFailed() + other.getFailed());
        setBroken(getBroken() + other.getBroken());
        setPassed(getPassed() + other.getPassed());
        setSkipped(getSkipped() + other.getSkipped());
        setUnknown(getUnknown() + other.getUnknown());
    }

    static Comparator<Statistic> comparator() {
        return Comparator.comparing(Statistic::getFailed)
                .thenComparing(Statistic::getBroken)
                .thenComparing(Statistic::getPassed)
                .thenComparing(Statistic::getSkipped)
                .thenComparing(Statistic::getUnknown);
    }

}
