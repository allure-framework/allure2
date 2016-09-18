package org.allurefw.report;

import org.allurefw.report.entity.TestCaseResult;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestCaseResultsReader {

    List<TestCaseResult> readResults(ResultsSource source);
}
