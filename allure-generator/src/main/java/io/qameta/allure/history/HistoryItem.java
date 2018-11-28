package io.qameta.allure.history;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Time;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Comparator;

import static java.util.Comparator.*;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class HistoryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String uid;
    protected String reportUrl;
    protected Status status;
    protected String statusDetails;
    protected Time time;

    public static Comparator<HistoryItem> comparingByTime() {
        return comparingByTimeAsc().reversed();
    }

    public static Comparator<HistoryItem> comparingByTimeAsc() {
        return comparing(HistoryItem::getTime,
                nullsFirst(comparing(Time::getStart, nullsFirst(naturalOrder())))
        );
    }
}
