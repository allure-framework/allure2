package org.allurefw.report.history;

import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.TestRunDetailsReader;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryPlugin extends AbstractPlugin {

    public static final String HISTORY_JSON = "history.json";

    public static final String HISTORY = "history";

    @Override
    protected void configure() {
        processor(HistoryProcessor.class);
        aggregator(HistoryAggregator.class).toReportData(HISTORY_JSON);

        Multibinder.newSetBinder(binder(), TestRunDetailsReader.class)
                .addBinding().to(HistoryReader.class);
    }
}
