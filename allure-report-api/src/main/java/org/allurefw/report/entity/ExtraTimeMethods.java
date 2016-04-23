package org.allurefw.report.entity;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 23.04.16
 */
//TODO what if we have only the duration??
public interface ExtraTimeMethods {

    long getStart();

    void setStart(Long value);

    long getStop();

    void setStop(Long value);

    default void merge(Time other) {
        if (other == null) {
            return;
        }

        setStart(Math.min(getStart(), other.getStart()));
        setStop(Math.max(getStop(), other.getStop()));
    }

    static Time create() {
        return new Time().withStart(0L).withStop(Long.MAX_VALUE);
    }
}
