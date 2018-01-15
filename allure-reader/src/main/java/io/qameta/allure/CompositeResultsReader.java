package io.qameta.allure;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class CompositeResultsReader implements ResultsReader {

    private final List<ResultsReader> readers;

    public CompositeResultsReader(final List<ResultsReader> readers) {
        this.readers = readers;
    }

    @Override
    public void readResults(final ResultsVisitor visitor, final Path file) {
        readers.forEach(reader -> reader.readResults(visitor, file));
    }
}
