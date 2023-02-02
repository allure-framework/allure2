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

import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.datetime.CompositeDateTimeParser;
import io.qameta.allure.datetime.DateTimeParser;
import io.qameta.allure.datetime.LocalDateTimeParser;
import io.qameta.allure.datetime.ZonedDateTimeParser;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.parser.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Plugin that reads data in JUnit.xml format.
 *
 * @since 2.0
 */
@SuppressWarnings({"PMD.ExcessiveImports", "ClassDataAbstractionCoupling", "ClassFanOutComplexity"})
public class JunitXmlPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JunitXmlPlugin.class);

    public static final String JUNIT_RESULTS_FORMAT = "junit";

    private static final BigDecimal MULTIPLICAND = new BigDecimal(1000);

    private static final String TEST_SUITE_ELEMENT_NAME = "testsuite";
    private static final String TEST_SUITES_ELEMENT_NAME = "testsuites";
    private static final String TEST_CASE_ELEMENT_NAME = "testcase";
    private static final String CLASS_NAME_ATTRIBUTE_NAME = "classname";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String VALUE_ATTRIBUTE_NAME = "value";
    private static final String TIME_ATTRIBUTE_NAME = "time";
    private static final String FAILURE_ELEMENT_NAME = "failure";
    private static final String ERROR_ELEMENT_NAME = "error";
    private static final String SKIPPED_ELEMENT_NAME = "skipped";
    private static final String MESSAGE_ATTRIBUTE_NAME = "message";
    private static final String RERUN_FAILURE_ELEMENT_NAME = "rerunFailure";
    private static final String RERUN_ERROR_ELEMENT_NAME = "rerunError";
    private static final String HOSTNAME_ATTRIBUTE_NAME = "hostname";
    private static final String TIMESTAMP_ATTRIBUTE_NAME = "timestamp";
    private static final String STATUS_ATTRIBUTE_NAME = "status";
    private static final String SKIPPED_ATTRIBUTE_VALUE = "notrun";
    private static final String SYSTEM_OUTPUT_ELEMENT_NAME = "system-out";
    private static final String PROPERTIES_ELEMENT_NAME = "properties";
    private static final String PROPERTY_ELEMENT_NAME = "property";

    private static final String XML_GLOB = "*.xml";

    private static final Map<String, Status> RETRIES;

    static {
        RETRIES = new HashMap<>();
        RETRIES.put(RERUN_FAILURE_ELEMENT_NAME, Status.FAILED);
        RETRIES.put(RERUN_ERROR_ELEMENT_NAME, Status.BROKEN);
    }

    private final DateTimeParser parser;

    public JunitXmlPlugin() {
        this(ZoneOffset.systemDefault());
    }

    public JunitXmlPlugin(final ZoneId defaultZoneId) {
        parser = new CompositeDateTimeParser(
                new ZonedDateTimeParser(),
                new LocalDateTimeParser(defaultZoneId)
        );
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override
    public void readResults(final Configuration configuration, final ResultsVisitor visitor, final Path directory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        listResults(directory).forEach(result -> parseRootElement(directory, result, context, visitor));
    }

    private void parseRootElement(final Path resultsDirectory, final Path parsedFile,
                                  final RandomUidContext context, final ResultsVisitor visitor) {
        try {
            LOGGER.debug("Parsing file {}", parsedFile);
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();

            final XmlElement rootElement = new XmlElement(builder.parse(parsedFile.toFile()).getDocumentElement());
            final String elementName = rootElement.getName();

            if (TEST_SUITE_ELEMENT_NAME.equals(elementName)) {
                parseTestSuite(rootElement, parsedFile, context, visitor, resultsDirectory);
                return;
            }
            if (TEST_SUITES_ELEMENT_NAME.equals(elementName)) {
                rootElement.get(TEST_SUITE_ELEMENT_NAME)
                        .forEach(element -> parseTestSuite(element, parsedFile, context, visitor, resultsDirectory));
                return;
            }
            LOGGER.debug("File {} is not a valid JUnit xml. Unknown root element {}", parsedFile, elementName);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", parsedFile, e);
        }
    }

    private void parseTestSuite(final XmlElement testSuiteElement, final Path parsedFile,
                                final RandomUidContext context, final ResultsVisitor visitor,
                                final Path resultsDirectory) {
        final String name = testSuiteElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String hostname = testSuiteElement.getAttribute(HOSTNAME_ATTRIBUTE_NAME);
        final String timestamp = testSuiteElement.getAttribute(TIMESTAMP_ATTRIBUTE_NAME);
        final TestSuiteInfo info = new TestSuiteInfo()
                .setName(name)
                .setHostname(hostname)
                .setTimestamp(getUnix(timestamp));
        testSuiteElement.get(TEST_CASE_ELEMENT_NAME)
                .forEach(element -> parseTestCase(info, element,
                                                  resultsDirectory, parsedFile, context, visitor));
    }

    private Long getUnix(final String timestamp) {
        if (isNull(timestamp)) {
            return null;
        }
        return parser.getEpochMilli(timestamp)
                .orElse(null);
    }

    private void parseTestCase(final TestSuiteInfo info, final XmlElement testCaseElement, final Path resultsDirectory,
                               final Path parsedFile, final RandomUidContext context, final ResultsVisitor visitor) {
        final String className = testCaseElement.getAttribute(CLASS_NAME_ATTRIBUTE_NAME);
        final Status status = getStatus(testCaseElement);
        final TestResult result = createStatuslessTestResult(info, testCaseElement, parsedFile, context);
        result.setStatus(status);
        result.setFlaky(isFlaky(testCaseElement));
        setStatusDetails(result, testCaseElement);
        final StageResult stageResult = new StageResult();
        getLogMessage(testCaseElement).ifPresent(logMessage -> {
            final List<String> lines = splitLines(logMessage);
            final List<Step> steps = lines
                    .stream()
                    .map(line -> new Step().setName(line))
                    .collect(Collectors.toList());
            stageResult.setSteps(steps);
        });
        getLogFile(resultsDirectory, className)
                .filter(Files::exists)
                .map(visitor::visitAttachmentFile)
                .map(attachment1 -> attachment1.setName("System out"))
                .ifPresent(attachment -> stageResult.setAttachments(singletonList(attachment)));
        result.setTestStage(stageResult);
        visitor.visitTestResult(result);

        RETRIES.forEach((elementName, retryStatus) -> testCaseElement.get(elementName).forEach(failure -> {
            final TestResult retried = createStatuslessTestResult(info, testCaseElement, parsedFile, context);
            retried.setHidden(true);
            retried.setStatus(retryStatus);
            retried.setStatusMessage(failure.getAttribute(MESSAGE_ATTRIBUTE_NAME));
            retried.setStatusTrace(failure.getValue());
            visitor.visitTestResult(retried);
        }));
    }

    private List<String> splitLines(final String str) {
        return Arrays.asList(str.split("\\r?\\n"));
    }

    private Optional<String> getLogMessage(final XmlElement testCaseElement) {
        return testCaseElement.getFirst(SYSTEM_OUTPUT_ELEMENT_NAME).map(XmlElement::getValue);
    }

    private Optional<Path> getLogFile(final Path resultsDirectory, final String className) {
        try {
            return Optional.ofNullable(className)
                    .map(name -> name + ".txt")
                    .map(resultsDirectory::resolve);
        } catch (InvalidPathException e) {
            LOGGER.debug("Can not find log file: invalid className {}", className, e);
            return Optional.empty();
        }
    }

    private TestResult createStatuslessTestResult(final TestSuiteInfo info, final XmlElement testCaseElement,
                                                  final Path parsedFile, final RandomUidContext context) {
        final String className = testCaseElement.getAttribute(CLASS_NAME_ATTRIBUTE_NAME);
        final Optional<String> suiteName = firstNotNull(info.getName(), className);
        final String name = testCaseElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String historyId = String.format("%s:%s#%s", info.getName(), className, name);
        final TestResult result = new TestResult();
        if (nonNull(className) && nonNull(name)) {
            result.setHistoryId(historyId);
        }
        result.setUid(context.getValue().get());
        result.setName(isNull(name) ? "Unknown test case" : name);
        result.setTime(getTime(info.getTimestamp(), testCaseElement, parsedFile));
        result.addLabelIfNotExists(RESULT_FORMAT, JUNIT_RESULTS_FORMAT);
        setParameters(result, testCaseElement);

        suiteName.ifPresent(s -> result.addLabelIfNotExists(LabelName.SUITE, s));
        if (nonNull(info.getHostname())) {
            result.addLabelIfNotExists(LabelName.HOST, info.getHostname());
        }
        if (nonNull(className)) {
            result.addLabelIfNotExists(LabelName.TEST_CLASS, className);
            result.addLabelIfNotExists(LabelName.PACKAGE, className);
        }
        return result;
    }

    private Status getStatus(final XmlElement testCaseElement) {
        if (testCaseElement.contains(FAILURE_ELEMENT_NAME)) {
            return Status.FAILED;
        }
        if (testCaseElement.contains(ERROR_ELEMENT_NAME)) {
            return Status.BROKEN;
        }
        if (testCaseElement.contains(SKIPPED_ELEMENT_NAME)) {
            return Status.SKIPPED;
        }

        if ((testCaseElement.containsAttribute(STATUS_ATTRIBUTE_NAME))
                && (testCaseElement.getAttribute(STATUS_ATTRIBUTE_NAME).equals(SKIPPED_ATTRIBUTE_VALUE))) {
            return Status.SKIPPED;
        }

        return Status.PASSED;
    }

    private void setStatusDetails(final TestResult result, final XmlElement testCaseElement) {
        Stream.of(FAILURE_ELEMENT_NAME, ERROR_ELEMENT_NAME, SKIPPED_ELEMENT_NAME)
                .filter(testCaseElement::contains)
                .map(testCaseElement::get)
                .filter(elements -> !elements.isEmpty())
                .flatMap(Collection::stream)
                .findFirst()
                .ifPresent(element -> {
                    //@formatter:off
                    result.setStatusMessage(element.getAttribute(MESSAGE_ATTRIBUTE_NAME));
                    result.setStatusTrace(element.getValue());
                    //@formatter:on
                });
    }

    private void setParameters(final TestResult result, final XmlElement testCaseElement) {
        testCaseElement
                .getFirst(PROPERTIES_ELEMENT_NAME)
                .map(properties -> properties.get(PROPERTY_ELEMENT_NAME))
                .map(Collection::stream)
                .map(stream -> stream.map(this::getParameter))
                .map(stream -> stream.collect(Collectors.toList()))
                .ifPresent(result::setParameters);
    }

    private Parameter getParameter(final XmlElement propertyElement) {
        final String name = propertyElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String value = propertyElement.getAttribute(VALUE_ATTRIBUTE_NAME);
        return new Parameter().setName(name).setValue(value);
    }

    private Time getTime(final Long suiteStart, final XmlElement testCaseElement, final Path parsedFile) {
        if (testCaseElement.containsAttribute(TIME_ATTRIBUTE_NAME)) {
            try {
                final long duration = BigDecimal.valueOf(testCaseElement.getDoubleAttribute(TIME_ATTRIBUTE_NAME))
                        .multiply(MULTIPLICAND)
                        .longValue();

                return nonNull(suiteStart)
                        ? new Time().setStart(suiteStart).setStop(suiteStart + duration).setDuration(duration)
                        : new Time().setDuration(duration);
            } catch (Exception e) {
                LOGGER.debug(
                        "Could not parse time attribute for element {} in file {}",
                        testCaseElement, parsedFile, e
                );
            }
        }
        return new Time();
    }

    private boolean isFlaky(final XmlElement testCaseElement) {
        return testCaseElement.contains(RERUN_ERROR_ELEMENT_NAME)
                || testCaseElement.contains(RERUN_FAILURE_ELEMENT_NAME);
    }

    private static List<Path> listResults(final Path directory) {
        final List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, XML_GLOB)) {
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

    private static Optional<String> firstNotNull(final String... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .findFirst();
    }
}
