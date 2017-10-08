package io.qameta.allure.trx;

import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
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
    public static final String TEST_NAME_ATTRIBUTE = "testName";
    public static final String START_TIME_ATTRIBUTE = "startTime";
    public static final String END_TIME_ATTRIBUTE = "endTime";
    public static final String OUTCOME_ATTRIBUTE = "outcome";
    public static final String TEST_DEFINITIONS_ELEMENT = "TestDefinitions";
    public static final String UNIT_TEST_ELEMENT = "UnitTest";
    public static final String NAME_ATTRIBUTE = "name";
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
        final String description = unitTestElement.getFirst(DESCRIPTION_ELEMENT)
                .map(XmlElement::getValue)
                .orElse(null);
        final String executionId = unitTestElement.getFirst(EXECUTION_ELEMENT)
                .map(execution -> execution.getAttribute(ID_ATTRIBUTE))
                .orElse(null);
        final Map<String, String> properties = parseProperties(unitTestElement);
        return new UnitTest(name, executionId, description, properties);
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
        getStatusDetails(unitTestResult).ifPresent(result::setStatusDetails);
        Optional.ofNullable(tests.get(executionId)).ifPresent(unitTest -> {
            result.setParameters(unitTest.getParameters());
            result.setDescription(unitTest.getDescription());
        });

        result.addLabelIfNotExists(RESULT_FORMAT, TRX_RESULTS_FORMAT);
        visitor.visitTestResult(result);
    }

    private Optional<StatusDetails> getStatusDetails(final XmlElement unitTestResult) {
        return unitTestResult.getFirst(OUTPUT_ELEMENT_NAME)
                .flatMap(output -> output.getFirst(ERROR_INFO_ELEMENT_NAME))
                .map(output -> {
                    final StatusDetails details = new StatusDetails();
                    output.getFirst(MESSAGE_ELEMENT_NAME).map(XmlElement::getValue).ifPresent(details::setMessage);
                    output.getFirst(STACK_TRACE_ELEMENT_NAME).map(XmlElement::getValue).ifPresent(details::setTrace);
                    return details;
                });
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
            default:
                return Status.UNKNOWN;
        }
    }

    protected Optional<Long> parseTime(final String time) {
        try {
            return Optional.ofNullable(time)
                    .map(ZonedDateTime::parse)
                    .map(ChronoZonedDateTime::toInstant)
                    .map(Instant::getEpochSecond);
        } catch (Exception e) {
            LOGGER.error("Could not parse time {}", time, e);
            return Optional.empty();
        }
    }

    private static List<Path> listResults(final Path directory) {
        List<Path> result = new ArrayList<>();
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
