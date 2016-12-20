package org.allurefw.report;

import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportInfo {

    private final Set<Plugin> plugins;

    private final Map<String, TestCase> testCases;

    private final List<TestCaseResult> results;

    public ReportInfo(Set<Plugin> plugins, Map<String, TestCase> testCases, List<TestCaseResult> results) {
        this.plugins = plugins;
        this.testCases = testCases;
        this.results = results;
    }

    public Set<Plugin> getPlugins() {
        return plugins;
    }

    public Map<String, TestCase> getTestCases() {
        return testCases;
    }

    public List<TestCaseResult> getResults() {
        return results;
    }
}
