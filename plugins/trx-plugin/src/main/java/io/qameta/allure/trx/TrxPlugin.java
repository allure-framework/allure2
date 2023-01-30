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
package io.qameta.allure.trx;

import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.parser.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.LabelName.TEST_CLASS;
import static java.nio.file.Files.newDirectoryStream;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class TrxPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrxPlugin.class);

    private static final String TEST_RUN_ELEMENT_NAME = "TestRun";

    public static final String TRX_RESULTS_FORMAT = "trx";
    public static final String RESULTS_ELEMENT_NAME = "Results";
    public static final String UNIT_TEST_RESULT_ELEMENT_NAME = "UnitTestResult";
    public static final String UNIT_TEST_INNER_RESULTS = "InnerResults";
    public static final String TEST_NAME_ATTRIBUTE = "testName";
    public static final String START_TIME_ATTRIBUTE = "startTime";
    public static final String END_TIME_ATTRIBUTE = "endTime";
    public static final String OUTCOME_ATTRIBUTE = "outcome";
    public static final String TEST_DEFINITIONS_ELEMENT = "TestDefinitions";
    public static final String UNIT_TEST_ELEMENT = "UnitTest";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TEST_METHOD_ELEMENT = "TestMethod";
    public static final String CLASS_NAME_ATTRIBUTE = "className";
    public static final String PROPERTIES_ELEMENT = "Properties";
    public static final String PROPERTY_ATTRIBUTE = "Property";
    public static final String KEY_ELEMENT = "Key";
    public static final String VALUE_ELEMENT = "Value";
    public static final String DESCRIPTION_ELEMENT = "Description";
    public static final String EXECUTION_ELEMENT = "Execution";
    public static final String ID_ATTRIBUTE = "id";
    public static final String EXECUTION_ID_ATTRIBUTE = "executionId";
    public static final String OUTPUT_ELEMENT_NAME = "Output";
    public static final String MESSAGE_ELEMENT_NAME = "Message";
    public static final String STACK_TRACE_ELEMENT_NAME = "StackTrace";
    public static final String ERROR_INFO_ELEMENT_NAME = "ErrorInfo";
    public static final String STDOUT_ELEMENT_NAME = "StdOut";

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        listResults(directory).forEach(result -> parseTestRun(result, context, visitor));
    }

    protected void parseTestRun(final Path parsedFile, final RandomUidContext context, final ResultsVisitor visitor) {
        try {
            LOGGER.debug("Parsing file {}", parsedFile);

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(parsedFile.toFile());
            final XmlElement testRunElement = new XmlElement(document.getDocumentElement());
            final String elementName = testRunElement.getName();
            if (!TEST_RUN_ELEMENT_NAME.equals(elementName)) {
                LOGGER.debug("{} is not a valid TRX file. Unknown root element {}", parsedFile, elementName);
                return;
            }
            final Map<String, UnitTest> tests = new HashMap<>();
            testRunElement.getFirst(TEST_DEFINITIONS_ELEMENT)
                    .ifPresent(testDefinitions -> {
                        testDefinitions.get(UNIT_TEST_ELEMENT).forEach(unitTestElement -> {
                            final UnitTest unitTest = parseUnitTest(unitTestElement);
                            tests.put(unitTest.getExecutionId(), unitTest);
                        });
                    });
            testRunElement.getFirst(RESULTS_ELEMENT_NAME)
                    .ifPresent(resultsElement -> parseResults(resultsElement, tests, context, visitor));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", parsedFile, e);
        }
    }

    protected UnitTest parseUnitTest(final XmlElement unitTestElement) {
        final String name = unitTestElement.getAttribute(NAME_ATTRIBUTE);
        final String className = unitTestElement.getFirst(TEST_METHOD_ELEMENT)
                .map(testMethod -> testMethod.getAttribute(CLASS_NAME_ATTRIBUTE))
                .orElse(null);
        final String description = unitTestElement.getFirst(DESCRIPTION_ELEMENT)
                .map(XmlElement::getValue)
                .orElse(null);
        final String executionId = unitTestElement.getFirst(EXECUTION_ELEMENT)
                .map(execution -> execution.getAttribute(ID_ATTRIBUTE))
                .orElse(null);
        final Map<String, String> properties = parseProperties(unitTestElement);
        return new UnitTest(name, className, executionId, description, properties);
    }

    private Map<String, String> parseProperties(final XmlElement unitTestElement) {
        final Map<String, String> properties = new HashMap<>();
        unitTestElement.getFirst(PROPERTIES_ELEMENT)
                .ifPresent(propertiesElement -> parseProperties(properties, propertiesElement));
        return properties;
    }

    private void parseProperties(final Map<String, String> properties, final XmlElement propertiesElement) {
        propertiesElement.get(PROPERTY_ATTRIBUTE)
                .forEach(propertyElement -> parseProperty(properties, propertyElement));
    }

    private void parseProperty(final Map<String, String> properties, final XmlElement propertyElement) {
        final Optional<String> key = propertyElement.getFirst(KEY_ELEMENT)
                .map(XmlElement::getValue);
        final Optional<String> value = propertyElement.getFirst(VALUE_ELEMENT)
                .map(XmlElement::getValue);
        if (key.isPresent() && value.isPresent()) {
            properties.put(key.get(), value.get());
        }
    }

    protected void parseResults(final XmlElement resultsElement,
                                final Map<String, UnitTest> tests,
                                final RandomUidContext context,
                                final ResultsVisitor visitor) {
        resultsElement.get(UNIT_TEST_RESULT_ELEMENT_NAME)
                .forEach(unitTestResult -> parseUnitTestResult(unitTestResult, tests, context, visitor));
    }

    protected void parseUnitTestResult(final XmlElement unitTestResult,
                                       final Map<String, UnitTest> tests,
                                       final RandomUidContext context,
                                       final ResultsVisitor visitor) {
        final String executionId = unitTestResult.getAttribute(EXECUTION_ID_ATTRIBUTE);
        final String testName = unitTestResult.getAttribute(TEST_NAME_ATTRIBUTE);
        final String startTime = unitTestResult.getAttribute(START_TIME_ATTRIBUTE);
        final String endTime = unitTestResult.getAttribute(END_TIME_ATTRIBUTE);
        final String outcome = unitTestResult.getAttribute(OUTCOME_ATTRIBUTE);
        final String uid = context.getValue().get();
        final TestResult result = new TestResult()
                .setUid(uid)
                .setName(testName)
                .setStatus(parseStatus(outcome))
                .setTime(getTime(startTime, endTime));
        getStatusMessage(unitTestResult).ifPresent(result::setStatusMessage);
        getStatusTrace(unitTestResult).ifPresent(result::setStatusTrace);
        getLogMessage(unitTestResult).ifPresent(logMessage -> {
            final List<String> lines = splitLines(logMessage);
            final List<Step> steps = lines
                    .stream()
                    .map(line -> new Step().setName(line))
                    .collect(Collectors.toList());
            final StageResult stageResult = new StageResult()
                    .setSteps(steps);
            result.setTestStage(stageResult);
        });
        Optional.ofNullable(tests.get(executionId)).ifPresent(unitTest -> {
            final String className = unitTest.getClassName();
            final String fullName = String.format("%s.%s", className, testName);
            result.setParameters(unitTest.getParameters());
            result.setDescription(unitTest.getDescription());
            result.setFullName(fullName);
            result.setHistoryId(fullName);
            result.addLabelIfNotExists(SUITE, className);
            result.addLabelIfNotExists(TEST_CLASS, className);
            result.addLabelIfNotExists(PACKAGE, className);
        });

        result.addLabelIfNotExists(RESULT_FORMAT, TRX_RESULTS_FORMAT);
        visitor.visitTestResult(result);

        unitTestResult.getFirst(UNIT_TEST_INNER_RESULTS)
            .ifPresent(innerResults -> {
                innerResults.get(UNIT_TEST_RESULT_ELEMENT_NAME)
                    .forEach(unitTestChildResult -> 
                        parseUnitTestResult(unitTestChildResult, tests, context, visitor, result.getLabels())
                    );
            }); 
    }

    protected void parseUnitTestResult(final XmlElement unitTestResult,
                                       final Map<String, UnitTest> tests,
                                       final RandomUidContext context,
                                       final ResultsVisitor visitor,
                                       final List<Label> labels) {
        final String testName = unitTestResult.getAttribute(TEST_NAME_ATTRIBUTE);
        final String startTime = unitTestResult.getAttribute(START_TIME_ATTRIBUTE);
        final String endTime = unitTestResult.getAttribute(END_TIME_ATTRIBUTE);
        final String outcome = unitTestResult.getAttribute(OUTCOME_ATTRIBUTE);
        final String uid = context.getValue().get();
        final TestResult result = new TestResult()
                .setUid(uid)
                .setName(testName)
                .setStatus(parseStatus(outcome))
                .setTime(getTime(startTime, endTime));
        getStatusMessage(unitTestResult).ifPresent(result::setStatusMessage);
        getStatusTrace(unitTestResult).ifPresent(result::setStatusTrace);
        getLogMessage(unitTestResult).ifPresent(logMessage -> {
            final List<String> lines = splitLines(logMessage);
            final List<Step> steps = lines
                    .stream()
                    .map(line -> new Step().setName(line))
                    .collect(Collectors.toList());
            final StageResult stageResult = new StageResult()
                    .setSteps(steps);
            result.setTestStage(stageResult);
        });

        labels.forEach(label -> result.addLabelIfNotExists(label.getName(), label.getValue()));

        visitor.visitTestResult(result);
        unitTestResult.getFirst(UNIT_TEST_INNER_RESULTS)
            .ifPresent(innerResults -> {
                innerResults.get(UNIT_TEST_RESULT_ELEMENT_NAME)
                    .forEach(unitTestChildResult -> 
                        parseUnitTestResult(unitTestChildResult, tests, context, visitor, result.getLabels())
                    );
            }); 
    }

    private List<String> splitLines(final String str) {
        return Arrays.asList(str.split("\\r?\\n"));
    }

    private Optional<String> getLogMessage(final XmlElement unitTestResult) {
        return unitTestResult.getFirst(OUTPUT_ELEMENT_NAME)
                .flatMap(output -> output.getFirst(STDOUT_ELEMENT_NAME))
                .map(XmlElement::getValue);
    }

    private Optional<String> getStatusMessage(final XmlElement unitTestResult) {
        return unitTestResult.getFirst(OUTPUT_ELEMENT_NAME)
                .flatMap(output -> output.getFirst(ERROR_INFO_ELEMENT_NAME))
                .flatMap(output -> output.getFirst(MESSAGE_ELEMENT_NAME))
                .map(XmlElement::getValue);
    }

    private Optional<String> getStatusTrace(final XmlElement unitTestResult) {
        return unitTestResult.getFirst(OUTPUT_ELEMENT_NAME)
                .flatMap(output -> output.getFirst(ERROR_INFO_ELEMENT_NAME))
                .flatMap(output -> output.getFirst(STACK_TRACE_ELEMENT_NAME))
                .map(XmlElement::getValue);
    }

    private Time getTime(final String startTime, final String endTime) {
        final Time time = new Time();
        parseTime(startTime).ifPresent(time::setStart);
        parseTime(endTime).ifPresent(time::setStop);
        if (Objects.nonNull(time.getStart()) && Objects.nonNull(time.getStop())) {
            time.setDuration(time.getStop() - time.getStart());
        }
        return time;
    }

    protected Status parseStatus(final String outcome) {
        if (Objects.isNull(outcome)) {
            return Status.UNKNOWN;
        }
        switch (outcome.toLowerCase()) {
            case "passed":
                return Status.PASSED;
            case "failed":
                return Status.FAILED;
            case "notexecuted":
                return Status.SKIPPED;
            default:
                return Status.UNKNOWN;
        }
    }

    protected Optional<Long> parseTime(final String time) {
        try {
            return Optional.ofNullable(time)
                    .map(ZonedDateTime::parse)
                    .map(ChronoZonedDateTime::toInstant)
                    .map(Instant::toEpochMilli);
        } catch (Exception e) {
            LOGGER.error("Could not parse time {}", time, e);
            return Optional.empty();
        }
    }

    private static List<Path> listResults(final Path directory) {
        final List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, "*.trx")) {
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
}
