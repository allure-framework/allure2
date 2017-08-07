package io.qameta.allure.junitxml;

import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@SuppressWarnings("PMD.ExcessiveImports")
public class JunitXmlPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JunitXmlPlugin.class);

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

    private static final Map<String, Status> RETRIES;


    static {
        RETRIES = new HashMap<>();
        RETRIES.put(RERUN_FAILURE_ELEMENT_NAME, Status.FAILED);
        RETRIES.put(RERUN_ERROR_ELEMENT_NAME, Status.BROKEN);
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
                rootElement.get(TEST_CASE_ELEMENT_NAME)
                        .forEach(element -> parseTestCase(element, resultsDirectory, parsedFile, context, visitor));
                return;
            }
            if (TEST_SUITES_ELEMENT_NAME.equals(elementName)) {
                rootElement.get("testsuite").stream()
                        .map(testSuiteElement -> testSuiteElement.get(TEST_CASE_ELEMENT_NAME))
                        .flatMap(Collection::stream)
                        .forEach(element -> parseTestCase(element, resultsDirectory, parsedFile, context, visitor));
                return;
            }
            LOGGER.debug("File {} is not a valid JUnit xml. Unknown root element {}", parsedFile, elementName);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", parsedFile, e);
        }
    }

    private void parseTestCase(final XmlElement testCaseElement, final Path resultsDirectory,
                               final Path parsedFile, final RandomUidContext context, final ResultsVisitor visitor) {
        final String className = testCaseElement.getAttribute(CLASS_NAME_ATTRIBUTE_NAME);
        final Status status = getStatus(testCaseElement);
        final TestResult result = createStatuslessTestResult(testCaseElement, parsedFile, context);
        result.setStatus(status);
        result.setStatusDetails(getStatusDetails(testCaseElement));

        getLogFile(resultsDirectory, className)
                .filter(Files::exists)
                .map(visitor::visitAttachmentFile)
                .map(attachment1 -> attachment1.setName("System out"))
                .ifPresent(attachment -> result.setTestStage(
                        new StageResult().setAttachments(singletonList(attachment))
                ));

        visitor.visitTestResult(result);

        RETRIES.forEach((elementName, retryStatus) -> testCaseElement.get(elementName).forEach(failure -> {
            final TestResult retried = createStatuslessTestResult(testCaseElement, parsedFile, context);
            retried.setHidden(true);
            retried.setStatus(retryStatus);
            retried.setStatusDetails(new StatusDetails()
                    .setMessage(failure.getAttribute(MESSAGE_ATTRIBUTE_NAME))
                    .setTrace(failure.getValue())
                    .setFlaky(true)
            );
            visitor.visitTestResult(retried);
        }));
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

    private TestResult createStatuslessTestResult(final XmlElement testCaseElement, final Path parsedFile,
                                                  final RandomUidContext context) {
        final String className = testCaseElement.getAttribute(CLASS_NAME_ATTRIBUTE_NAME);
        final String name = testCaseElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String historyId = String.format("%s#%s", className, name);
        final TestResult result = new TestResult();
        if (nonNull(className) && nonNull(name)) {
            result.setHistoryId(historyId);
        }
        result.setUid(context.getValue().get());
        result.setName(isNull(name) ? "Unknown test case" : name);
        result.setTime(getTime(testCaseElement, parsedFile));
        result.addLabelIfNotExists(RESULT_FORMAT, JUNIT_RESULTS_FORMAT);

        if (nonNull(className)) {
            result.addLabelIfNotExists(LabelName.SUITE, className);
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
        return Status.PASSED;
    }

    private StatusDetails getStatusDetails(final XmlElement testCaseElement) {
        final boolean flaky = isFlaky(testCaseElement);
        return Stream.of(FAILURE_ELEMENT_NAME, ERROR_ELEMENT_NAME, SKIPPED_ELEMENT_NAME)
                .filter(testCaseElement::contains)
                .map(testCaseElement::get)
                .filter(elements -> !elements.isEmpty())
                .flatMap(Collection::stream)
                .findFirst()
                .map(element -> new StatusDetails()
                        .setMessage(element.getAttribute(MESSAGE_ATTRIBUTE_NAME))
                        .setTrace(element.getValue())
                        .setFlaky(flaky))
                .orElseGet(() -> new StatusDetails().setFlaky(flaky));
    }

    private Time getTime(final XmlElement testCaseElement, final Path parsedFile) {
        if (testCaseElement.containsAttribute(TIME_ATTRIBUTE_NAME)) {
            try {
                final long duration = BigDecimal.valueOf(testCaseElement.getDoubleAttribute(TIME_ATTRIBUTE_NAME))
                        .multiply(MULTIPLICAND)
                        .longValue();
                return new Time().setDuration(duration);
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
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, "TEST-*.xml")) {
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
