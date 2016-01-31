package org.allurefw.report.io;

import com.google.common.collect.ImmutableSet;
import org.allurefw.report.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public abstract class AbstractTestCaseGroupsIterator<T, S> implements Iterator<TestCase> {

    private final Iterator<T> groups;

    private Iterator<TestCase> current = ImmutableSet.<TestCase>of().iterator();

    public AbstractTestCaseGroupsIterator(Iterator<T> groups) {
        this.groups = groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        if (current.hasNext()) {
            return true;
        }

        if (!groups.hasNext()) {
            return false;
        }

        current = groupIterator(groups.next());
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

    protected abstract AbstractTestCaseGroupIterator<T, S> groupIterator(T group);

}
