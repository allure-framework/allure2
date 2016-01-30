package org.allurefw.report.allure1;

import org.allurefw.report.TestCase;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public class TestCaseResultIterator implements Iterator<TestCase> {

    private final Iterator<TestCaseResult> iterator;

    public TestCaseResultIterator(TestSuiteResult testSuite) {
        this.iterator = testSuite.getTestCases().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCase next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        TestCaseResult result = iterator.next();

        return new TestCase().withName(result.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
