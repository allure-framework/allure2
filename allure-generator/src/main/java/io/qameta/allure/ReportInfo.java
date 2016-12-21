package io.qameta.allure;

import io.qameta.allure.Plugin;
import io.qameta.allure.entity.TestCase;
import io.qameta.allure.entity.TestCaseResult;

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
