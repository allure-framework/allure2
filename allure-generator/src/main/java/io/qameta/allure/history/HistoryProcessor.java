package io.qameta.allure.history;

import io.qameta.allure.Processor;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.HashMap;
import java.util.Map;

import static io.qameta.allure.history.HistoryPlugin.HISTORY;
import static io.qameta.allure.history.HistoryPlugin.copy;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryProcessor implements Processor {

    @Override
    public void process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        Map<String, HistoryData> history = testRun.getExtraBlock(HISTORY, new HashMap<>());
        HistoryData data = history.computeIfAbsent(
                result.getId(),
                id -> new HistoryData().withId(id).withName(result.getName())
        );
        data.updateStatistic(result);
        result.addExtraBlock(HISTORY, copy(data));
    }
}
