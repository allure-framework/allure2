package org.allurefw.report.junit;

import org.allurefw.report.io.AbstractTestCaseGroupsIterator;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.Iterator;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public class TestSuitesIterator
        extends AbstractTestCaseGroupsIterator<Testsuite, Testsuite.Testcase> {

    /**
     * {@inheritDoc}
     */
    public TestSuitesIterator(Iterator<Testsuite> groups) {
        super(groups);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TestSuiteIterator groupIterator(Testsuite testSuite) {
        return new TestSuiteIterator(testSuite);
    }
}
