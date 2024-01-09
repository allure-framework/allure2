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
package io.qameta.allure.junitxml;

import java.math.BigDecimal;

public final class JunitXmlConstants {
    public static final String JUNIT_RESULTS_FORMAT = "junit";

    public static final BigDecimal MULTIPLICAND = new BigDecimal(1000);
    public static final String TEST_SUITE_ELEMENT_NAME = "testsuite";
    public static final String TEST_SUITES_ELEMENT_NAME = "testsuites";
    public static final String TEST_CASE_ELEMENT_NAME = "testcase";
    public static final String CLASS_NAME_ATTRIBUTE_NAME = "classname";
    public static final String NAME_ATTRIBUTE_NAME = "name";
    public static final String VALUE_ATTRIBUTE_NAME = "value";
    public static final String TIME_ATTRIBUTE_NAME = "time";
    public static final String FAILURE_ELEMENT_NAME = "failure";
    public static final String ERROR_ELEMENT_NAME = "error";
    public static final String SKIPPED_ELEMENT_NAME = "skipped";
    public static final String MESSAGE_ATTRIBUTE_NAME = "message";
    public static final String RERUN_FAILURE_ELEMENT_NAME = "rerunFailure";
    public static final String RERUN_ERROR_ELEMENT_NAME = "rerunError";
    public static final String HOSTNAME_ATTRIBUTE_NAME = "hostname";
    public static final String TIMESTAMP_ATTRIBUTE_NAME = "timestamp";
    public static final String STATUS_ATTRIBUTE_NAME = "status";
    public static final String SKIPPED_ATTRIBUTE_VALUE = "notrun";
    public static final String SYSTEM_OUTPUT_ELEMENT_NAME = "system-out";
    public static final String PROPERTIES_ELEMENT_NAME = "properties";
    public static final String PROPERTY_ELEMENT_NAME = "property";
    public static final String XML_GLOB = "*.xml";
    public static final String TXT_EXTENSION = ".txt";

    private JunitXmlConstants() { }
}
