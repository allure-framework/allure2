package io.qameta.allure.entity;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
public interface WithTime {

    void setTime(Time time);

    Time getTime();

    default void setTime(Long start, Long stop) {
        setTime(new Time()
                .withStart(start)
                .withStop(stop)
                .withDuration((start == null || stop == null) ? null : stop - start)
        );
    }

    default void setTime(Long duration) {
        setTime(new Time()
                .withDuration(duration)
        );
    }
}
