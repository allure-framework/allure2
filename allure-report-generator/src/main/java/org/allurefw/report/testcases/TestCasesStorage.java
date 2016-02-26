package org.allurefw.report.testcases;

import com.google.common.collect.Iterables;
import org.allurefw.report.entity.TestCase;

import java.util.Collections;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class TestCasesStorage {

    private Iterable<TestCase> testCases = Collections.emptyList();

    public void addTestCase(Iterable<TestCase> testCases) {
        this.testCases = Iterables.concat(this.testCases, testCases);
    }

    public Iterable<TestCase> getTestCases() {
        return testCases;
    }
}
