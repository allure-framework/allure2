package io.qameta.allure.history;

import io.qameta.allure.ResultAggregator;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.history.HistoryPlugin.HISTORY;
import static io.qameta.allure.history.HistoryPlugin.copy;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryResultAggregator implements ResultAggregator<Map<String, HistoryData>> {

    @Override
    public Supplier<Map<String, HistoryData>> supplier(final TestRun testRun, final TestCase testCase) {
        return () -> {
            final Map<String, HistoryData> result = new HashMap<>();
            final Map<String, HistoryData> history = testRun.getExtraBlock(HISTORY, new HashMap<>());
            history.forEach((key, value) -> result.put(key, copy(value)));
            return result;
        };
    }

    @Override
    public Consumer<Map<String, HistoryData>> aggregate(final TestRun testRun,
                                                        final TestCase testCase,
                                                        final TestCaseResult result) {
        return (history) -> {
            if (Objects.isNull(result.getTestCaseId())) {
                return;
            }

            //@formatter:off
            final HistoryData data = history.computeIfAbsent(
                result.getTestCaseId(),
                id -> new HistoryData().withId(id).withName(result.getName())
            );
            //@formatter:on
            data.updateStatistic(result);

            final HistoryItem newItem = new HistoryItem()
                    .withStatus(result.getStatus())
                    .withStatusDetails(result.getStatusMessage().orElse(null))
                    .withTime(result.getTime())
                    .withTestRunName(testRun.getName());

            final List<HistoryItem> newItems = Stream.concat(Stream.of(newItem), data.getItems().stream())
                    .limit(5)
                    .collect(Collectors.toList());
            data.setItems(newItems);
        };
    }
}
