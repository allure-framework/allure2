package org.allurefw.report.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.allurefw.report.ReportApiUtils.listFilesSafe;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public abstract class AbstractResultsReaderIterator<T> implements Iterator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResultsReaderIterator.class);

    private final Iterator<Path> files;

    /**
     * Creates an instance of iterator.
     */
    public AbstractResultsReaderIterator(Path... resultDirectories) {
        files = listFilesSafe(getFilesGlob(), resultDirectories).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return files.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        if (!files.hasNext()) {
            throw new NoSuchElementException();
        }
        Path next = files.next();
        try {
            return readResult(next);
        } catch (IOException e) {
            LOGGER.warn(String.format("Could not read <%s> file", next.toAbsolutePath().toString()), e);
            return next();
        } finally {
            //TODO remove this debug logging
            System.out.println(getClass() + ": read file " + next);
        }
    }

    /**
     * Read result from given path.
     *
     * @throws IOException if any occurs.
     */
    protected abstract T readResult(Path path) throws IOException;

    /**
     * Returns the glob for files to read.
     */
    protected abstract String getFilesGlob();

}
