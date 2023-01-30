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

import io.qameta.allure.Reader;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Timeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmlwise.Plist;
import xmlwise.XmlParseException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUITE;
import static java.util.Collections.emptyList;

/**
 * @author charlie (Dmitry Baev).
 */
public class XcTestPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(XcTestPlugin.class);

    public static final String XCTEST_RESULTS_FORMAT = "xctest";

    private static final String TESTABLE_SUMMARIES = "TestableSummaries";
    private static final String TESTS = "Tests";
    private static final String SUB_TESTS = "Subtests";
    private static final String TITLE = "Title";
    private static final String SUB_ACTIVITIES = "SubActivities";
    private static final String ACTIVITY_SUMMARIES = "ActivitySummaries";
    private static final String HAS_SCREENSHOT = "HasScreenshotData";

    private static final String ATTACHMENTS = "Attachments";
    private static final String ATTACHMENT_FILENAME = "Filename";


    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final List<Path> testSummaries = listSummaries(directory);
        testSummaries.forEach(summaryPath -> readSummaries(directory, summaryPath, visitor));
    }

    private void readSummaries(final Path directory, final Path testSummariesPath, final ResultsVisitor visitor) {
        try {
            LOGGER.info("Parse file {}", testSummariesPath);
            final Map<String, Object> loaded = Plist.load(testSummariesPath.toFile());
            final List<?> summaries = asList(loaded.getOrDefault(TESTABLE_SUMMARIES, emptyList()));
            summaries.forEach(summary -> parseSummary(directory, summary, visitor));
        } catch (XmlParseException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", testSummariesPath, e);
        }
    }

    private void parseSummary(final Path directory, final Object summary, final ResultsVisitor visitor) {
        final Map<String, Object> props = asMap(summary);
        final String name = ResultsUtils.getTestName(props);
        final List<Object> tests = asList(props.getOrDefault(TESTS, emptyList()));
        tests.forEach(test -> parseTestSuite(name, test, directory, visitor));
    }

    @SuppressWarnings("unchecked")
    private void parseTestSuite(final String parentName, final Object testSuite,
                                final Path directory, final ResultsVisitor visitor) {
        final Map<String, Object> props = asMap(testSuite);
        if (ResultsUtils.isTest(props)) {
            parseTest(parentName, testSuite, directory, visitor);
            return;
        }

        final List<?> subTests = asList(props.getOrDefault(SUB_TESTS, emptyList()));
        subTests.forEach(subTest -> parseTestSuite(ResultsUtils.getTestName(props), subTest, directory, visitor));
    }

    private void parseTest(final String suiteName, final Object test,
                           final Path directory, final ResultsVisitor visitor) {
        final Map<String, Object> props = asMap(test);
        final TestResult result = ResultsUtils.getTestResult(props);
        result.addLabelIfNotExists(RESULT_FORMAT, XCTEST_RESULTS_FORMAT);
        result.addLabelIfNotExists(SUITE, suiteName);

        asList(props.getOrDefault(ACTIVITY_SUMMARIES, emptyList()))
                .forEach(activity -> parseStep(directory, result, activity, visitor));
        final Optional<Step> lastFailedStep = result.getTestStage().getSteps().stream()
                .filter(s -> !s.getStatus().equals(Status.PASSED)).sorted((a, b) -> -1).findFirst();
        lastFailedStep.map(Step::getStatusMessage).ifPresent(result::setStatusMessage);
        lastFailedStep.map(Step::getStatusTrace).ifPresent(result::setStatusTrace);
        visitor.visitTestResult(result);
    }

    private void parseStep(final Path directory, final Object parent,
                           final Object activity, final ResultsVisitor visitor) {

        final Map<String, Object> props = asMap(activity);
        final String activityTitle = (String) props.get(TITLE);

        if (activityTitle.startsWith("Start Test at")) {
            getStartTime(activityTitle).ifPresent(start -> {
                final Timeable withTime = (Timeable) parent;
                final long duration = withTime.getTime().getDuration();
                withTime.getTime().setStart(start);
                withTime.getTime().setStop(start + duration);
            });
            return;
        }
        final Step step = ResultsUtils.getStep(props);
        if (activityTitle.startsWith("Assertion Failure:")) {
            step.setStatusMessage(activityTitle);
            step.setStatus(Status.FAILED);
        }

        if (props.containsKey(HAS_SCREENSHOT)) {
            addScreenshots(directory, visitor, props, step);
        }

        if (props.containsKey(ATTACHMENTS)) {
            addAttachments(directory, visitor, props, step);
        }

        if (parent instanceof TestResult) {
            ((TestResult) parent).getTestStage().getSteps().add(step);
        }

        if (parent instanceof Step) {
            ((Step) parent).getSteps().add(step);
        }

        asList(props.getOrDefault(SUB_ACTIVITIES, emptyList()))
                .forEach(subActivity -> parseStep(directory, step, subActivity, visitor));

        final Optional<Step> lastFailedStep = step.getSteps().stream()
                .filter(s -> !s.getStatus().equals(Status.PASSED)).sorted((a, b) -> -1).findFirst();
        lastFailedStep.map(Step::getStatus).ifPresent(step::setStatus);
        lastFailedStep.map(Step::getStatusMessage).ifPresent(step::setStatusMessage);
        lastFailedStep.map(Step::getStatusTrace).ifPresent(step::setStatusTrace);
    }

    private void addScreenshots(final Path directory,
                                final ResultsVisitor visitor,
                                final Map<String, Object> props,
                                final Step step) {
        final String uuid = props.get("UUID").toString();
        final Path attachments = directory.resolve(ATTACHMENTS);
        Stream.of("jpg", "png")
                .map(ext -> attachments.resolve(String.format("Screenshot_%s.%s", uuid, ext)))
                .filter(Files::exists)
                .map(visitor::visitAttachmentFile)
                .forEach(step.getAttachments()::add);
    }

    private void addAttachments(final Path directory,
                                final ResultsVisitor visitor,
                                final Map<String, Object> props,
                                final Step step) {
        final Path attachments = directory.resolve(ATTACHMENTS);
        asList(props.get(ATTACHMENTS)).stream()
                .map(this::asMap)
                .map(p -> p.get(ATTACHMENT_FILENAME).toString())
                .map(attachments::resolve)
                .filter(Files::exists)
                .map(visitor::visitAttachmentFile)
                .forEach(step.getAttachments()::add);
    }

    @SuppressWarnings("unchecked")
    private List<Object> asList(final Object object) {
        return List.class.cast(object);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(final Object object) {
        return Map.class.cast(object);
    }

    private static List<Path> listSummaries(final Path directory) {
        final List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, "*.plist")) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    result.add(path);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read data from {}: {}", directory, e);
        }
        return result;
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

}
