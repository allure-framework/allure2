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
package io.qameta.allure.xctest;

import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The collection of Test result utils methods.
 */
@SuppressWarnings("MultipleStringLiterals")
public final class ResultsUtils {

    private static final String TEST_NAME = "TestName";
    private static final String TEST_STATUS = "TestStatus";
    private static final String TEST_DURATION = "Duration";
    private static final String TEST_IDENTIFIER = "TestIdentifier";

    private static final String STEP_NAME = "Title";
    private static final String STEP_START_TIME = "StartTimeInterval";
    private static final String STEP_STOP_TIME = "FinishTimeInterval";

    private ResultsUtils() {
    }

    public static TestResult getTestResult(final Map<String, Object> props) {
        return new TestResult()
                .setUid(UUID.randomUUID().toString())
                .setName(ResultsUtils.getTestName(props))
                .setStatus(ResultsUtils.getTestStatus(props))
                .setFullName(ResultsUtils.getFullName(props))
                .setTime(ResultsUtils.getTestTime(props))
                .setTestStage(new StageResult());
    }

    public static boolean isTest(final Map<String, Object> props) {
        return props.containsKey(TEST_STATUS);
    }

    public static String getTestName(final Map<String, Object> props) {
        return (String) props.getOrDefault(TEST_NAME, "Unknown");
    }

    private static String getFullName(final Map<String, Object> props) {
        return (String) props.getOrDefault(TEST_IDENTIFIER, "Unknown");
    }

    private static Time getTestTime(final Map<String, Object> props) {
        return new Time().setDuration(parseTime(props.getOrDefault(TEST_DURATION, "0").toString()));
    }

    private static Status getTestStatus(final Map<String, Object> props) {
        final Object status = props.get(TEST_STATUS);
        if (Objects.isNull(status)) {
            return Status.UNKNOWN;
        }
        if ("Success".equals(status)) {
            return Status.PASSED;
        }
        if ("Failure".equals(status)) {
            return Status.FAILED;
        }
        return Status.UNKNOWN;
    }


    public static Step getStep(final Map<String, Object> props) {
        return new Step()
                .setName(getStepName(props))
                .setTime(getStepTime(props))
                .setStatus(Status.PASSED);
    }

    private static String getStepName(final Map<String, Object> props) {
        return (String) props.getOrDefault(STEP_NAME, "Unknown");
    }

    private static Time getStepTime(final Map<String, Object> props) {
        final long start = parseTime(props.getOrDefault(STEP_START_TIME, "0").toString());
        final long stop = parseTime(props.getOrDefault(STEP_STOP_TIME, "0").toString());
        return new Time().setStart(start).setStop(stop).setDuration(stop - start);
    }

    private static long parseTime(final String time) {
        final Double doubleTime = Double.parseDouble(time);
        final int seconds = doubleTime.intValue();
        return seconds * 1000;
    }
}
