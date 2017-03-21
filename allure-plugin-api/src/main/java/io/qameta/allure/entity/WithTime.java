package io.qameta.allure.entity;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithTime {

    void setTime(final Time time);

    default void setTime(final Long start, final Long stop) {
        setTime(new Time()
                .withStart(start)
                .withStop(stop)
                .withDuration((start == null || stop == null) ? null : stop - start)
        );
    }

    default void setTime(final Long duration) {
        setTime(new Time()
                .withDuration(duration)
        );
    }

    Time getTime();
}
