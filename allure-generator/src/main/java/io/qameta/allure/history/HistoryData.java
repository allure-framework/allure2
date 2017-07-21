package io.qameta.allure.history;

import io.qameta.allure.entity.Statistic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@Data
@Accessors(chain = true)
public class HistoryData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Statistic statistic = new Statistic();
    protected List<HistoryItem> items = new ArrayList<>();
}
