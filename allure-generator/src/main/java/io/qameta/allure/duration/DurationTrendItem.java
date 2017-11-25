package io.qameta.allure.duration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Timeable;
import io.qameta.allure.trend.TrendItem;

/**
 * @author charlie (Dmitry Baev).
 */
public class DurationTrendItem extends TrendItem {

    private static final String DURATION_KEY = "duration";

    @JsonIgnore
    private final GroupTime time = new GroupTime();

    public void updateTime(final Timeable timeable) {
        time.update(timeable);
        if (time.getDuration() != null) {
            setMetric(DURATION_KEY, time.getDuration());
        } else {
            setMetric(DURATION_KEY, 0L);
        }
    }

}
