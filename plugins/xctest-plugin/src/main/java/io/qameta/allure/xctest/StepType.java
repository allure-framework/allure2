package io.qameta.allure.xctest;

/**
 * Technical steps types.
 */
public enum StepType {
    FEATURE("allure.feature"),
    ISSUE("allure.issue"),
    TMS("allure.tms"),
    STORY("allure.story"),
    EPIC("allure.epic");

    private final String value;

    StepType(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
