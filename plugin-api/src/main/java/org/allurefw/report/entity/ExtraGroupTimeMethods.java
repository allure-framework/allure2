package org.allurefw.report.entity;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author charlie (Dmitry Baev).
 */
public interface ExtraGroupTimeMethods {

    Long getStart();

    void setStart(Long start);

    Long getStop();

    void setStop(Long stop);

    Long getDuration();

    void setDuration(Long duration);

    Long getMaxDuration();

    void setMaxDuration(Long maxDuration);

    Long getSumDuration();

    void setSumDuration(Long sumDuration);

    default void merge(GroupTime groupTime) {
        if (groupTime == null) {
            return;
        }
        update(firstNonNull(getStart(), Long.MAX_VALUE), groupTime.getStart(), Math::min, this::setStart);
        update(firstNonNull(getStop(), 0L), groupTime.getStop(), Math::max, this::setStop);
        update(getStop(), getStart(), (a, b) -> a - b, this::setDuration);
        update(firstNonNull(getMaxDuration(), 0L), groupTime.getMaxDuration(), Math::max, this::setMaxDuration);
        update(firstNonNull(getSumDuration(), 0L), groupTime.getSumDuration(), (a, b) -> a + b, this::setSumDuration);
    }

    default void update(Time time) {
        if (time == null) {
            return;
        }
        update(firstNonNull(getStart(), Long.MAX_VALUE), time.getStart(), Math::min, this::setStart);
        update(firstNonNull(getStop(), 0L), time.getStop(), Math::max, this::setStop);
        update(getStop(), getStart(), (a, b) -> a - b, this::setDuration);
        update(firstNonNull(getMaxDuration(), 0L), time.getDuration(), Math::max, this::setMaxDuration);
        update(firstNonNull(getSumDuration(), 0L), time.getDuration(), (a, b) -> a + b, this::setSumDuration);
    }

    static <T> void update(T first, T second, BiFunction<T, T, T> merge, Consumer<T> setter) {
        if (first != null && second != null) {
            setter.accept(merge.apply(first, second));
        }
    }
}
