package org.allurefw.report;

import java.nio.file.Path;

/**
 * A factory to create different types of {@link ResultsSource}.
 *
 * @author charlie (Dmitry Baev).
 * @since 2.0
 */
public interface ResultsSourceFactory {

    /**
     * Creates an instance of FileSystemResultsSource.
     *
     * @param resultsDirectory the directory to read results from.
     * @return created results source.
     */
    ResultsSource create(Path resultsDirectory);

}
