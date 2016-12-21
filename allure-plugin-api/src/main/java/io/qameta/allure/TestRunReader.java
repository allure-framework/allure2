package io.qameta.allure;

import io.qameta.allure.entity.TestRun;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestRunReader {

    TestRun readTestRun(Path source);

}
