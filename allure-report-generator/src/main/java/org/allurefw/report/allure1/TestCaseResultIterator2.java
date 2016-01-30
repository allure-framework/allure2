package org.allurefw.report.allure1;

import com.google.common.collect.ImmutableSet;
import org.allurefw.report.TestCase;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public class TestCaseResultIterator2 implements Iterator<TestCase> {

    private final Iterator<TestSuiteResult> testSuites;

    private Iterator<TestCase> current = ImmutableSet.<TestCase>of().iterator();

    public TestCaseResultIterator2(Iterator<TestSuiteResult> testSuites) {
        this.testSuites = testSuites;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        if (current.hasNext()) {
            return true;
        }

        if (!testSuites.hasNext()) {
            return false;
        }

        current = new TestCaseResultIterator(testSuites.next());
        return hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestCase next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return current.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
