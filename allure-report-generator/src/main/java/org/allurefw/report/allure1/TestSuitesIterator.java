package org.allurefw.report.allure1;

import org.allurefw.report.io.AbstractTestCaseGroupsIterator;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.Iterator;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public class TestSuitesIterator
        extends AbstractTestCaseGroupsIterator<TestSuiteResult, TestCaseResult> {

    /**
     * {@inheritDoc}
     */
    public TestSuitesIterator(Iterator<TestSuiteResult> groups) {
        super(groups);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TestSuiteIterator groupIterator(TestSuiteResult testSuite) {
        return new TestSuiteIterator(testSuite);
    }
}
