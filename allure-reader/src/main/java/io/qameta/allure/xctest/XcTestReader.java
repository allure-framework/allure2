package io.qameta.allure.xctest;

import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.entity.TestResultStep;
import io.qameta.allure.entity.TestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmlwise.Plist;
import xmlwise.XmlParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUITE;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

/**
 * @author charlie (Dmitry Baev).
 */
public class XcTestReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(XcTestReader.class);

    public static final String XCTEST_RESULTS_FORMAT = "xctest";

    private static final String TESTABLE_SUMMARIES = "TestableSummaries";
    private static final String TESTS = "Tests";
    private static final String SUB_TESTS = "Subtests";
    private static final String TITLE = "Title";
    private static final String SUB_ACTIVITIES = "SubActivities";
    private static final String ACTIVITY_SUMMARIES = "ActivitySummaries";
    private static final String HAS_SCREENSHOT = "HasScreenshotData";

    private static final String TEST_NAME = "TestName";
    private static final String TEST_STATUS = "TestStatus";
    private static final String TEST_DURATION = "Duration";
    private static final String TEST_IDENTIFIER = "TestIdentifier";

    private static final String STEP_NAME = "Title";
    private static final String STEP_START_TIME = "StartTimeInterval";
    private static final String STEP_STOP_TIME = "FinishTimeInterval";


    @Override
    public void readResults(final ResultsVisitor visitor, final Path file) {
        if (file.getFileName().toString().endsWith(".plist")) {
            readSummaries(visitor, file);
        }
    }

    private void readSummaries(final ResultsVisitor visitor, final Path testSummariesPath) {
        try {
            LOGGER.info("Parse file {}", testSummariesPath);
            final Map<String, Object> loaded = Plist.load(testSummariesPath.toFile());
            final List<?> summaries = asList(loaded.getOrDefault(TESTABLE_SUMMARIES, emptyList()));
            summaries.forEach(summary -> parseSummary(visitor, summary));
        } catch (XmlParseException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", testSummariesPath, e);
        }
    }

    private void parseSummary(final ResultsVisitor visitor, final Object summary) {
        final Map<String, Object> props = asMap(summary);
        final String name = getTestName(props);
        final List<Object> tests = asList(props.getOrDefault(TESTS, emptyList()));
        tests.forEach(test -> parseTestSuite(visitor, name, test));
    }

    @SuppressWarnings("unchecked")
    private void parseTestSuite(final ResultsVisitor visitor,
                                final String parentName,
                                final Object testSuite) {
        final Map<String, Object> props = asMap(testSuite);
        if (isTest(props)) {
            parseTest(visitor, parentName, testSuite);
            return;
        }

        final List<?> subTests = asList(props.getOrDefault(SUB_TESTS, emptyList()));
        subTests.forEach(subTest -> parseTestSuite(visitor, getTestName(props), subTest));
    }

    private void parseTest(final ResultsVisitor visitor,
                           final String suiteName,
                           final Object test) {
        final Map<String, Object> props = asMap(test);
        final TestResult result = getTestResult(props);
        result.addLabelIfNotExists(RESULT_FORMAT, XCTEST_RESULTS_FORMAT);
        result.addLabelIfNotExists(SUITE, suiteName);

        final TestResult stored = visitor.visitTestResult(result);

        final TestResultExecution execution = new TestResultExecution();
        asList(props.getOrDefault(ACTIVITY_SUMMARIES, emptyList()))
                .forEach(activity -> parseStep(visitor, execution, activity));

        //TODO result changed after store
        Optional<TestResultStep> lastFailedStep = execution.getSteps().stream()
                .filter(s -> !s.getStatus().equals(TestStatus.PASSED))
                .reduce((first, second) -> second);
        lastFailedStep.map(TestResultStep::getMessage).ifPresent(result::setMessage);
        lastFailedStep.map(TestResultStep::getTrace).ifPresent(result::setTrace);

        visitor.visitTestResultExecution(stored.getId(), execution);
    }

    private void parseStep(final ResultsVisitor visitor, final Object parent,
                           final Object activity) {

        final Map<String, Object> props = asMap(activity);
        final String activityTitle = (String) props.get(TITLE);

        if (activityTitle.startsWith("Start Test at")) {
            getStartTime(activityTitle).ifPresent(start -> setTime(parent, start));
            return;
        }
        final TestResultStep step = getStep(props);
        if (activityTitle.startsWith("Assertion Failure:")) {
            step.setMessage(activityTitle);
            step.setStatus(TestStatus.FAILED);
        }

        if (props.containsKey(HAS_SCREENSHOT)) {
            addAttachment(visitor, props, step);
        }

        if (parent instanceof TestResultExecution) {
            ((TestResultExecution) parent).getSteps().add(step);
        }

        if (parent instanceof TestResultStep) {
            ((TestResultStep) parent).getSteps().add(step);
        }

        asList(props.getOrDefault(SUB_ACTIVITIES, emptyList()))
                .forEach(subActivity -> parseStep(visitor, step, subActivity));

        Optional<TestResultStep> lastFailedStep = step.getSteps().stream()
                .filter(s -> !s.getStatus().equals(TestStatus.PASSED))
                .reduce((first, second) -> second);

        lastFailedStep.map(TestResultStep::getStatus).ifPresent(step::setStatus);
        lastFailedStep.map(TestResultStep::getMessage).ifPresent(step::setMessage);
        lastFailedStep.map(TestResultStep::getTrace).ifPresent(step::setTrace);
    }

    private void addAttachment(final ResultsVisitor visitor,
                               final Map<String, Object> props,
                               final TestResultStep step) {
        //TODO attachments support
//        String uuid = props.get("UUID").toString();
//        Path attachments = directory.resolve("Attachments");
//        Stream.of("jpg", "png")
//                .map(ext -> attachments.resolve(String.format("Screenshot_%s.%s", uuid, ext)))
//                .filter(Files::exists)
//                .map(visitor::visitAttachmentFile)
//                .forEach(step.getAttachments()::add);
    }

    @SuppressWarnings("unchecked")
    private List<Object> asList(final Object object) {
        return List.class.cast(object);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(final Object object) {
        return Map.class.cast(object);
    }

    private static Optional<Long> getStartTime(final String stepName) {
        try {
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX", Locale.US);
            final Date date = dateFormat.parse(stepName.substring(14));
            return Optional.of(date.getTime());
        } catch (DateTimeException | ParseException e) {
            return Optional.empty();
        }
    }

    private static void setTime(final Object testOrStep, final Long start) {
        if (testOrStep instanceof TestResult) {
            final TestResult test = (TestResult) testOrStep;
            test.setStart(start);
            test.setStop(getStop(start, test.getDuration()));
        }

        if (testOrStep instanceof TestResultStep) {
            final TestResultStep step = (TestResultStep) testOrStep;
            step.setStart(start);
            step.setStop(getStop(start, step.getDuration()));
        }
    }

    private static Long getStop(final Long start, final Long duration) {
        if (nonNull(start) && nonNull(duration)) {
            return start + duration;
        }
        return null;
    }

    public static TestResult getTestResult(final Map<String, Object> props) {
        return new TestResult()
                .setName(getTestName(props))
                .setStatus(getTestStatus(props))
                .setFullName(getFullName(props))
                .setDuration(getTestDuration(props));
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
                .map(XcTestReader::parseTime)
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
                .map(XcTestReader::parseTime)
                .orElse(null);
    }

    private static Long getStepStop(final Map<String, Object> props) {
        return Optional.ofNullable(props.get(STEP_STOP_TIME))
                .map(Objects::toString)
                .map(XcTestReader::parseTime)
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