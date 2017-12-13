package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

import static java.util.Objects.isNull;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class Time implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long start;
    protected Long stop;
    protected Long duration;

    public static Time create(final Long duration) {
        return new Time().setDuration(duration);
    }

    public static Time create(final Long start, final Long stop) {
        return new Time()
                .setStart(start)
                .setStop(stop)
                .setDuration(isNull(start) || isNull(stop) ? null : stop - start);
    }

}
