package io.qameta.allure;

import io.qameta.allure.service.TestResultService;

import java.io.IOException;
import java.nio.file.Path;

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
     * @param service         the service that can be used to access report data.
     * @param outputDirectory the report directory.
     * @throws IOException if any occurs.
     */
    void aggregate(TestResultService service,
                   Path outputDirectory) throws IOException;

}
