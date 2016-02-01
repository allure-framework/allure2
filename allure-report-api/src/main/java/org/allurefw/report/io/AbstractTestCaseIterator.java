package org.allurefw.report.io;

import com.google.common.collect.ImmutableSet;
import org.allurefw.report.entity.TestCase;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public abstract class AbstractTestCaseIterator<T, S> implements Iterator<TestCase> {

    private final Iterator<T> groups;

    private Iterator<TestCase> current = ImmutableSet.<TestCase>of().iterator();

    public AbstractTestCaseIterator(Path[] resultDirectories) {
        this.groups = createReader(resultDirectories);
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

        current = new TestCaseGroupIterator(groups.next());
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
     * Get result reader iterator.
     */
    protected abstract Iterator<T> createReader(Path... resultDirectories);

    /**
     * Extract test cases from the group.
     */
    protected abstract Iterator<S> extract(T group);

    /**
     * Convert external test case to {@link TestCase}
     */
    protected abstract TestCase convert(S source);

    /**
     * Internal test case iterator inside the groups file.
     */
    protected class TestCaseGroupIterator implements Iterator<TestCase> {

        protected final Iterator<S> iterator;

        public TestCaseGroupIterator(T group) {
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
    }
}
