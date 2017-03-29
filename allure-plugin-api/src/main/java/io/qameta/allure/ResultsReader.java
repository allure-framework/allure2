package io.qameta.allure;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface ResultsReader {

    void readResults(Configuration configuration, ResultsVisitor visitor, Path directory);

}
