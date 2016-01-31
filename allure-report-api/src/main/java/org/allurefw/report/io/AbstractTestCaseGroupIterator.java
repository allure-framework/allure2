package org.allurefw.report.io;

import org.allurefw.report.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public abstract class AbstractTestCaseGroupIterator<T, S> implements Iterator<TestCase> {

    protected final Iterator<S> iterator;

    public AbstractTestCaseGroupIterator(T group) {
        this.iterator = extract(group);
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

        S result = iterator.next();
        return convert(result);
    }

    /**
     * Extract test cases from the group.
     */
    protected abstract Iterator<S> extract(T group);

    /**
     * Convert external test case to {@link TestCase}
     */
    protected abstract TestCase convert(S source);
}
