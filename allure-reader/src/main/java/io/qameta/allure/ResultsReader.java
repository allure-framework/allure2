package io.qameta.allure;

import java.nio.file.Path;

/**
 * Reader extension. Can read some data from results folder add store into storage using
 * {@link ResultsVisitor}.
 *
 * @since 2.0
 */
@FunctionalInterface
public interface ResultsReader extends Extension {

    /**
     * Process results directory.
     *
     * @param visitor the visitor to store data into results storage.
     * @param file    the result file to process.
     */
    void readResults(ResultsVisitor visitor, Path file);

}
