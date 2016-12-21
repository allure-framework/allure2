package io.qameta.allure.history;

import com.google.inject.multibindings.Multibinder;
import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.TestRunDetailsReader;
import io.qameta.allure.entity.Statistic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryPlugin extends AbstractPlugin {

    public static final String HISTORY_JSON = "history.json";

    public static final String HISTORY = "history";

    @Override
    protected void configure() {
        processor(HistoryProcessor.class);
        aggregateResults(HistoryResultAggregator.class).toReportData(HISTORY_JSON);

        Multibinder.newSetBinder(binder(), TestRunDetailsReader.class)
                .addBinding().to(HistoryReader.class);
    }

    public static HistoryData copy(HistoryData other) {
        Statistic statistic = new Statistic();
        statistic.merge(other.getStatistic());
        List<HistoryItem> items = new ArrayList<>(other.getItems());
        return new HistoryData()
                .withId(other.getId())
                .withName(other.getName())
                .withStatistic(statistic)
                .withItems(items);
    }
}
