package org.allurefw.report.history;

import org.allurefw.report.Processor;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.HashMap;
import java.util.Map;

import static org.allurefw.report.history.HistoryPlugin.HISTORY;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryProcessor implements Processor {

    @Override
    public void process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        Map<String, HistoryData> history = testRun.getExtraBlock(HISTORY, new HashMap<>());
        result.addExtraBlock(HISTORY, history.computeIfAbsent(
                result.getId(),
                id -> new HistoryData().withId(id).withName(result.getName()))
        );
    }
}
