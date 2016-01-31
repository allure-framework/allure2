package org.allurefw.report.io;

import com.google.common.collect.ImmutableSet;
import org.allurefw.report.TestCase;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public abstract class AbstractTestCaseReader<T, S> implements Iterable<TestCase> {

    private final Path[] resultDirectories;

    public AbstractTestCaseReader(Path[] resultDirectories) {
        this.resultDirectories = resultDirectories;
    }

    @Override
    public Iterator<TestCase> iterator() {
        return new TestCaseGroupsIterator(read(resultDirectories));
    }

    /**
     * Get result reader iterator.
     */
    protected abstract Iterator<T> read(Path... resultDirectories);

    /**
     * Extract test cases from the group.
     */
    protected abstract Iterator<S> extract(T group);

    /**
     * Convert external test case to {@link TestCase}
     */
    protected abstract TestCase convert(S source);


    /**
     * Internal test case groups iterator.
     */
    protected class TestCaseGroupsIterator implements Iterator<TestCase> {

        private final Iterator<T> groups;

        private Iterator<TestCase> current = ImmutableSet.<TestCase>of().iterator();

        public TestCaseGroupsIterator(Iterator<T> groups) {
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

    }

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
