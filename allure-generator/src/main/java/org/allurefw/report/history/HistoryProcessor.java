package org.allurefw.report.history;

import org.allurefw.report.Processor;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.HashMap;
import java.util.Map;

import static org.allurefw.report.history.HistoryPlugin.HISTORY;
import static org.allurefw.report.history.HistoryPlugin.copy;

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
