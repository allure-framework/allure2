/*
 *  Copyright 2016-2024 Qameta Software Inc
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

    // the absolute reference date of 1 Jan 2001 00:00:00 GMT.
    private static final Long APPLE_ABSOLUTE_TIME_OFFSET = 978_307_200L;

    private ResultsUtils() {
    }

    public static TestResult getTestResult(final Map<String, Object> props) {
        return new TestResult()
                .setUid(UUID.randomUUID().toString())
                .setName(getTestName(props))
                .setStatus(getTestStatus(props))
                .setFullName(getFullName(props))
                .setTime(getTestTime(props))
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
        final double duration = (double) props.getOrDefault(TEST_DURATION, (double) 0);
        return new Time().setDuration(Double.valueOf(duration * 1000).longValue());
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
                .setTime(getActivityTime(props))
                .setStatus(Status.PASSED);
    }

    private static String getStepName(final Map<String, Object> props) {
        return (String) props.getOrDefault(STEP_NAME, "Unknown");
    }

    private static Time getActivityTime(final Map<String, Object> props) {
        final Double startTimeInterval = (Double) props.get(STEP_START_TIME);
        final Double finishTimeInterval = (Double) props.get(STEP_STOP_TIME);

        if (Objects.isNull(startTimeInterval) && Objects.isNull(finishTimeInterval)) {
            return null;
        }

        final Long start = convertAppleTime(startTimeInterval);
        final Long stop = convertAppleTime(finishTimeInterval);
        return Time.create(
                start,
                Objects.isNull(stop) ? start : stop
        );
    }


    private static Long convertAppleTime(final Double startTimeInterval) {
        if (Objects.isNull(startTimeInterval)) {
            return null;
        }

        return APPLE_ABSOLUTE_TIME_OFFSET + startTimeInterval.longValue();
    }
}
