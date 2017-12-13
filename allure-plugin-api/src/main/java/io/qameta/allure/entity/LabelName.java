package io.qameta.allure.entity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * @author charlie (Dmitry Baev).
 */
public enum LabelName implements Serializable {

    OWNER("owner"),
    SEVERITY("severity"),
    ISSUE("issue"),
    TAG("tag"),
    TEST_TYPE("testType"),
    PARENT_SUITE("parentSuite"),
    SUITE("suite"),
    SUB_SUITE("subSuite"),
    PACKAGE("package"),
    EPIC("epic"),
    FEATURE("feature"),
    STORY("story"),
    TEST_CLASS("testClass"),
    TEST_METHOD("testMethod"),
    HOST("host"),
    THREAD("thread"),
    LANGUAGE("language"),
    FRAMEWORK("framework"),
    RESULT_FORMAT("resultFormat");

    private static final long serialVersionUID = 1L;

    private final String value;

    LabelName(final String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public Label label(final String value) {
        return new Label().setName(value()).setValue(value);
    }
}
