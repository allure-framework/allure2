package org.allurefw.report.history;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryAggregator implements Aggregator<Map<String, List<HistoryItem>>> {

    @Override
    public Supplier<Map<String, List<HistoryItem>>> supplier() {
        return HashMap::new;
    }

    @Override
    public Consumer<Map<String, List<HistoryItem>>> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return (history) -> {
            List<HistoryItem> items = history.computeIfAbsent(result.getId(), id -> new ArrayList<>());
            items.add(new HistoryItem(
                    result.getStatus(),
                    Objects.isNull(result.getFailure()) ? null : result.getFailure().getMessage(),
                    result.getTime().getStop())
            );
        };
    }
}
