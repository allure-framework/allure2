package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;

import java.nio.file.Path;

/**
 * Reader extension. Can read some data from results folder add store into storage using
 * {@link ResultsVisitor}.
 *
 * @since 2.0
 */
@FunctionalInterface
public interface Reader extends Extension {

    /**
     * Process results directory.
     *
     * @param configuration the report configuration.
     * @param visitor       the visitor to store data into results storage.
     * @param directory     the results directory to process.
     */
    void readResults(Configuration configuration, ResultsVisitor visitor, Path directory);

}
