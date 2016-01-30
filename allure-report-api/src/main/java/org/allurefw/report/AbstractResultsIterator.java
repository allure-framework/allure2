package org.allurefw.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.nio.file.Files.newDirectoryStream;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.10.15
 */
public abstract class AbstractResultsIterator<T> implements Iterator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResultsIterator.class);

    private final Iterator<Path> files;

    /**
     * Creates an instance of iterator.
     */
    public AbstractResultsIterator(Path... resultDirectories) {
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

    /**
     * The safe wrapper for {@link #listFilesSafe(String, Path...)}
     */
    public static List<Path> listFilesSafe(String glob, Path... directories) {
        try {
            return listFiles(glob, directories);
        } catch (IOException e) {
            LOGGER.error("Could not find any files by glob '{}': {}", glob, e);
            return Collections.emptyList();
        }
    }

    /**
     * Find all files by glob in specified directories.
     *
     * @param directories the directory to find suite files.
     * @return the list of found test suite files.
     * @throws IOException if any occurs.
     */
    public static List<Path> listFiles(String glob, Path... directories) throws IOException {
        List<Path> result = new ArrayList<>();
        for (Path directory : directories) {
            result.addAll(listFiles(glob, directory));
        }
        return result;
    }

    /**
     * Find all files by glob in specified directory.
     *
     * @param directory the directory to find suite files.
     * @return the list of found test suite files.
     * @throws IOException if any occurs.
     */
    public static List<Path> listFiles(String glob, Path directory) throws IOException {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, glob)) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    result.add(path);
                }
            }
        }
        return result;
    }
}
