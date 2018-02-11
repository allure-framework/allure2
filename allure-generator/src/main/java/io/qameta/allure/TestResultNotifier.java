package io.qameta.allure;

import io.qameta.allure.event.TestResultCreated;
import io.qameta.allure.listener.TestResultListener;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultNotifier implements TestResultListener {

    private final List<TestResultListener> listeners;

    public TestResultNotifier(final List<TestResultListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onTestResultCreated(final TestResultCreated event) {
        listeners.forEach(listener -> listener.onTestResultCreated(event));
    }
}
