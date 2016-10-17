package org.allurefw.report.history;

import org.allurefw.report.Processor;
import org.allurefw.report.entity.Status;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.Arrays;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryProcessor implements Processor {

    @Override
    public TestCaseResult process(TestRun testRun, TestCase testCase, TestCaseResult result) {
        List<HistoryItem> history = Arrays.asList(
                new HistoryItem(Status.PASSED, null, 1412949540660L),
                new HistoryItem(Status.PASSED, null, 1412949520660L),
                new HistoryItem(Status.FAILED, "Demo history fail", 1412949541660L)
        );
        result.addExtraBlock("history", history);
        return result;
    }
}
