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
    public Supplier<Map<String, HistoryData>> supplier(TestRun testRun, TestCase testCase) {
        return () -> {
            Map<String, HistoryData> result = new HashMap<>();
            Map<String, HistoryData> history = testRun.getExtraBlock(HISTORY, new HashMap<>());
            history.forEach((key, value) -> result.put(key, copy(value)));
            return result;
        };
    }

    @Override
    public Consumer<Map<String, HistoryData>> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return (history) -> {
            HistoryData data = history.computeIfAbsent(
                    result.getId(),
                    id -> new HistoryData().withId(id).withName(result.getName())
            );
            data.updateStatistic(result);

            HistoryItem newItem = new HistoryItem()
                    .withStatus(result.getStatus())
                    .withStatusDetails(Objects.isNull(result.getFailure()) ? null : result.getFailure().getMessage())
                    .withTimestamp(result.getTime().getStop())
                    .withTestRunName(testRun.getName());

            List<HistoryItem> newItems = Stream.concat(Stream.of(newItem), data.getItems().stream())
                    .limit(5)
                    .collect(Collectors.toList());
            data.setItems(newItems);
        };
    }
}
