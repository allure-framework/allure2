package io.qameta.allure;

import io.qameta.allure.entity.TestCaseResult;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface ResultsReader {

    List<TestCaseResult> readResults(Path source);
}
