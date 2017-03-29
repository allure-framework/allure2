package io.qameta.allure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Base report plugin. Can be used to process results and/or generate
 * some data to report directory.
 *
 * @since 2.0
 */
@FunctionalInterface
public interface Plugin {

    /**
     * Process report data.
     *
     * @param configuration   the report configuration.
     * @param launches        all the parsed test results.
     * @param outputDirectory the report directory.
     * @throws IOException if any occurs.
     */
    void process(Configuration configuration,
                 List<LaunchResults> launches,
                 Path outputDirectory) throws IOException;

}
