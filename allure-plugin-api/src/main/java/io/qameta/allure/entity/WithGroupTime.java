package io.qameta.allure.entity;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WithGroupTime {

    GroupTime getTime();

    void setTime(GroupTime time);

    default void updateTime(GroupTime groupTime) {
        getTimeSafe().merge(groupTime);
    }

    default void updateTime(Timeable timed) {
        getTimeSafe().update(timed.getTime());
    }

    default GroupTime getTimeSafe() {
        if (getTime() == null) {
            setTime(new GroupTime());
        }
        return getTime();
    }
}
