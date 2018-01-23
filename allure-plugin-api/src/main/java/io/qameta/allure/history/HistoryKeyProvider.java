package io.qameta.allure.history;

import io.qameta.allure.entity.TestResult;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface HistoryKeyProvider {

    String getHistoryKey(TestResult testResult);

}
