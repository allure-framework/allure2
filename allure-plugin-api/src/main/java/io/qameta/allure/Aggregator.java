package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Aggregator extension. Can be used to process results and/or generate
 * some data to report directory.
 *
 * @since 2.0
 */
@FunctionalInterface
public interface Aggregator extends Extension {

    /**
     * Process report data.
     *
     * @param configuration   the report configuration.
     * @param launchesResults all the parsed test results.
     * @param outputDirectory the report directory.
     * @throws IOException if any occurs.
     */
    void aggregate(Configuration configuration,
                   List<LaunchResults> launchesResults,
                   Path outputDirectory) throws IOException;

}
