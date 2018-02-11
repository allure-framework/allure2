package io.qameta.allure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class CompositeResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeResultsReader.class);

    private final List<ResultsReader> readers;

    public CompositeResultsReader(final List<ResultsReader> readers) {
        this.readers = readers;
    }

    @Override
    public void readResultFile(final ResultsVisitor visitor, final Path file) {
        for (ResultsReader reader : readers) {
            try {
                reader.readResultFile(visitor, file);
            } catch (Exception e) {
                LOGGER.error("Could not parse result file {}", file, e);
            }
        }
    }
}
