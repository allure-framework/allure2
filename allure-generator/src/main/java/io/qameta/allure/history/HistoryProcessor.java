package io.qameta.allure.history;

import io.qameta.allure.Processor;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.TestRun;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.qameta.allure.history.HistoryPlugin.HISTORY;
import static io.qameta.allure.history.HistoryPlugin.copy;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryProcessor implements Processor {

    @Override
    public void process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        Map<String, HistoryData> history = testRun.getExtraBlock(HISTORY, new HashMap<>());
        String testCaseId = result.getTestCaseId();
        if (Objects.isNull(testCaseId) || !history.containsKey(testCaseId)) {
            return;
        }

        HistoryData data = copy(history.get(testCaseId));
        data.updateStatistic(result);
        result.addExtraBlock(HISTORY, data);
    }
}
