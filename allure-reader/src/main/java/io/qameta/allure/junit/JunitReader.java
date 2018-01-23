package io.qameta.allure.junit;

import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.entity.TestStatus;
import io.qameta.allure.parser.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.util.ConvertUtils.firstNonNullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author charlie (Dmitry Baev).
 */
public class JunitReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JunitReader.class);

    public static final String JUNIT_RESULTS_FORMAT = "junit";

    private static final BigDecimal MULTIPLICAND = new BigDecimal(1000);

    private static final String TEST_SUITE_ELEMENT_NAME = "testsuite";
    private static final String TEST_SUITES_ELEMENT_NAME = "testsuites";
    private static final String TEST_CASE_ELEMENT_NAME = "testcase";
    private static final String CLASS_NAME_ATTRIBUTE_NAME = "classname";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String TIME_ATTRIBUTE_NAME = "time";
    private static final String FAILURE_ELEMENT_NAME = "failure";
    private static final String ERROR_ELEMENT_NAME = "error";
    private static final String SKIPPED_ELEMENT_NAME = "skipped";
    private static final String MESSAGE_ATTRIBUTE_NAME = "message";
    private static final String RERUN_FAILURE_ELEMENT_NAME = "rerunFailure";
    private static final String RERUN_ERROR_ELEMENT_NAME = "rerunError";
    private static final String HOSTNAME_ATTRIBUTE_NAME = "hostname";
    private static final String TIMESTAMP_ATTRIBUTE_NAME = "timestamp";

    private static final Map<String, TestStatus> RETRIES;

    static {
        RETRIES = new HashMap<>();
        RETRIES.put(RERUN_FAILURE_ELEMENT_NAME, TestStatus.FAILED);
        RETRIES.put(RERUN_ERROR_ELEMENT_NAME, TestStatus.BROKEN);
    }

    @Override
    public void readResultFile(final ResultsVisitor visitor, final Path file) {
        if (file.getFileName().toString().endsWith(".xml")) {
            parseRootElement(visitor, file);
        }
    }

    private void parseRootElement(final ResultsVisitor visitor, final Path parsedFile) {
        try {
            LOGGER.debug("Parsing file {}", parsedFile);
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();

            final XmlElement rootElement = new XmlElement(builder.parse(parsedFile.toFile()).getDocumentElement());
            final String elementName = rootElement.getName();

            if (TEST_SUITE_ELEMENT_NAME.equals(elementName)) {
                parseTestSuite(visitor, rootElement, parsedFile);
                return;
            }
            if (TEST_SUITES_ELEMENT_NAME.equals(elementName)) {
                rootElement.get(TEST_SUITE_ELEMENT_NAME)
                        .forEach(element -> parseTestSuite(visitor, element, parsedFile));
                return;
            }
            LOGGER.debug("File {} is not a valid JUnit xml. Unknown root element {}", parsedFile, elementName);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", parsedFile, e);
        }
    }

    private void parseTestSuite(final ResultsVisitor visitor,
                                final XmlElement testSuiteElement,
                                final Path parsedFile) {
        final String name = testSuiteElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String hostname = testSuiteElement.getAttribute(HOSTNAME_ATTRIBUTE_NAME);
        final String timestamp = testSuiteElement.getAttribute(TIMESTAMP_ATTRIBUTE_NAME);
        final JunitTestSuite info = new JunitTestSuite()
                .setName(name)
                .setHostname(hostname)
                .setTimestamp(getUnix(timestamp));
        testSuiteElement.get(TEST_CASE_ELEMENT_NAME)
                .forEach(element -> parseTestCase(visitor, element, parsedFile, info
                ));
    }

    private Long getUnix(final String timestamp) {
        if (isNull(timestamp)) {
            return null;
        }
        final LocalDateTime parsed = LocalDateTime.parse(timestamp);
        return parsed.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    private void parseTestCase(final ResultsVisitor visitor, final XmlElement testCaseElement,
                               final Path parsedFile, final JunitTestSuite info) {
        final String className = testCaseElement.getAttribute(CLASS_NAME_ATTRIBUTE_NAME);
        final TestStatus status = getStatus(testCaseElement);
        final TestResult result = createStatuslessTestResult(info, testCaseElement, parsedFile);
        result.setStatus(status);
        result.setFlaky(isFlaky(testCaseElement));
        setStatusDetails(result, testCaseElement);

        final TestResult stored = visitor.visitTestResult(result);

        final AttachmentLink systemOut = new AttachmentLink()
                .setFileName(className + ".txt")
                .setContentType("text/plain")
                .setName("System out");

        final TestResultExecution execution = new TestResultExecution();
        execution.getAttachments().add(systemOut);
        visitor.visitTestResultExecution(stored.getId(), execution);

        RETRIES.forEach((elementName, retryStatus) -> testCaseElement.get(elementName).forEach(failure -> {
            final TestResult retried = createStatuslessTestResult(info, testCaseElement, parsedFile);
            retried.setHidden(true);
            retried.setStatus(retryStatus);
            retried.setMessage(failure.getAttribute(MESSAGE_ATTRIBUTE_NAME));
            retried.setTrace(failure.getValue());
            visitor.visitTestResult(retried);
        }));
    }

    private TestResult createStatuslessTestResult(final JunitTestSuite info, final XmlElement testCaseElement,
                                                  final Path parsedFile) {
        final String className = testCaseElement.getAttribute(CLASS_NAME_ATTRIBUTE_NAME);
        final Optional<String> suiteName = firstNonNullSafe(info.getName(), className);
        final String name = testCaseElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String historyId = String.format("%s:%s#%s", info.getName(), className, name);
        final TestResult result = new TestResult();
        if (nonNull(className) && nonNull(name)) {
            result.setHistoryKey(historyId);
        }
        result.setName(isNull(name) ? "Unknown test case" : name);
        result.setStart(info.getTimestamp());
        final Long duration = getDuration(testCaseElement, parsedFile);
        result.setDuration(duration);
        if (nonNull(duration) && nonNull(info.getTimestamp())) {
            result.setStop(info.getTimestamp() + duration);
        }
        result.addLabelIfNotExists(RESULT_FORMAT, JUNIT_RESULTS_FORMAT);

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

    private TestStatus getStatus(final XmlElement testCaseElement) {
        if (testCaseElement.contains(FAILURE_ELEMENT_NAME)) {
            return TestStatus.FAILED;
        }
        if (testCaseElement.contains(ERROR_ELEMENT_NAME)) {
            return TestStatus.BROKEN;
        }
        if (testCaseElement.contains(SKIPPED_ELEMENT_NAME)) {
            return TestStatus.SKIPPED;
        }
        return TestStatus.PASSED;
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
                    result.setMessage(element.getAttribute(MESSAGE_ATTRIBUTE_NAME));
                    result.setTrace(element.getValue());
                    //@formatter:on
                });
    }

    private Long getDuration(final XmlElement testCaseElement, final Path parsedFile) {
        if (testCaseElement.containsAttribute(TIME_ATTRIBUTE_NAME)) {
            try {
                return BigDecimal.valueOf(testCaseElement.getDoubleAttribute(TIME_ATTRIBUTE_NAME))
                        .multiply(MULTIPLICAND)
                        .longValue();
            } catch (Exception e) {
                LOGGER.debug(
                        "Could not parse time attribute for element {} in file {}",
                        testCaseElement, parsedFile, e
                );
            }
        }
        return null;
    }

    private boolean isFlaky(final XmlElement testCaseElement) {
        return testCaseElement.contains(RERUN_ERROR_ELEMENT_NAME)
                || testCaseElement.contains(RERUN_FAILURE_ELEMENT_NAME);
    }
}
