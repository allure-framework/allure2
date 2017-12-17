package io.qameta.allure.entity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
public enum TestResultType implements Serializable {

    TEST("test"),
    BEFORE_FIXTURE("before_fixture"),
    AFTER_FIXTURE("after_fixture");

    private static final long serialVersionUID = 1L;

    private final String value;

    TestResultType(final String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

}
