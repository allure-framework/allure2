package io.qameta.allure.xctest;

import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultStep;
import io.qameta.allure.entity.TestStatus;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

/**
 * The collection of Test result utils methods.
 */
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
                .setId(UUID.randomUUID().toString())
                .setName(ResultsUtils.getTestName(props))
                .setStatus(ResultsUtils.getTestStatus(props))
                .setFullName(ResultsUtils.getFullName(props))
                .setDuration(ResultsUtils.getTestDuration(props))
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

    private static Long getTestDuration(final Map<String, Object> props) {
        return Optional.ofNullable(props.get(TEST_DURATION))
                .map(Objects::toString)
                .map(ResultsUtils::parseTime)
                .orElse(null);
    }

    private static TestStatus getTestStatus(final Map<String, Object> props) {
        final Object status = props.get(TEST_STATUS);
        if (Objects.isNull(status)) {
            return TestStatus.UNKNOWN;
        }
        if ("Success".equals(status)) {
            return TestStatus.PASSED;
        }
        if ("Failure".equals(status)) {
            return TestStatus.FAILED;
        }
        return TestStatus.UNKNOWN;
    }


    public static TestResultStep getStep(final Map<String, Object> props) {
        final Long start = getStepStart(props);
        final Long stop = getStepStop(props);
        return new TestResultStep()
                .setName(getStepName(props))
                .setStart(start)
                .setStop(stop)
                .setDuration(getDuration(start, stop))
                .setStatus(TestStatus.PASSED);
    }

    private static String getStepName(final Map<String, Object> props) {
        return (String) props.getOrDefault(STEP_NAME, "Unknown");
    }

    private static Long getStepStart(final Map<String, Object> props) {
        return Optional.ofNullable(props.get(STEP_START_TIME))
                .map(Objects::toString)
                .map(ResultsUtils::parseTime)
                .orElse(null);
    }

    private static Long getStepStop(final Map<String, Object> props) {
        return Optional.ofNullable(props.get(STEP_STOP_TIME))
                .map(Objects::toString)
                .map(ResultsUtils::parseTime)
                .orElse(null);
    }

    private static long parseTime(final String time) {
        final Double doubleTime = Double.parseDouble(time);
        final int seconds = doubleTime.intValue();
        return seconds * 1000;
    }

    private static Long getDuration(final Long start, final Long stop) {
        return nonNull(start) && nonNull(stop) ? stop - start : null;
    }
}