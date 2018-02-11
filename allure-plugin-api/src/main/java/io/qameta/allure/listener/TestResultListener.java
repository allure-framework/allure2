package io.qameta.allure.listener;

import io.qameta.allure.event.TestResultCreated;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TestResultListener {

    void onTestResultCreated(TestResultCreated event);

}
