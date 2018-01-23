package io.qameta.allure.logging;

import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class LoggingResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingResultsReader.class);

    @Override
    public void readResultFile(final ResultsVisitor visitor, final Path file) {
        LOGGER.info("Process results file {}", file);
    }

}
