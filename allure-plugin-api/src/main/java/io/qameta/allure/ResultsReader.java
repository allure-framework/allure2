package io.qameta.allure;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public interface ResultsReader {

    void readResults(ReportConfiguration configuration, final ResultsVisitor visitor, final Path directory);

}
