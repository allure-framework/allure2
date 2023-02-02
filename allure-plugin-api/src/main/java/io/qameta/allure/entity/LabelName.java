/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
