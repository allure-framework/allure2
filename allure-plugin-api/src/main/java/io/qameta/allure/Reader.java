package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface Reader {

    void readResults(Configuration configuration, ResultsVisitor visitor, Path directory);

}
