package org.allurefw.report.entity;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithTime {

    void setTime(Time time);

    Time getTime();

    default void setTime(long start, long stop) {
        setTime(new Time()
                .withStart(start)
                .withStop(stop)
                .withDuration(stop - start)
        );
    }

    default void setTime(long duration) {
        setTime(new Time()
                .withDuration(duration)
        );
    }
}
