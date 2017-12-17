package io.qameta.allure.entity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
public enum TestStatus implements Serializable {

    FAILED("failed"),
    BROKEN("broken"),
    PASSED("passed"),
    SKIPPED("skipped"),
    UNKNOWN("unknown");

    private static final long serialVersionUID = 1L;

    private final String value;

    TestStatus(final String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

}
