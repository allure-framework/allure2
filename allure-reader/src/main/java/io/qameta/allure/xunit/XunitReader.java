package io.qameta.allure.xunit;

import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.TestParameter;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestStatus;
import io.qameta.allure.parser.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.FRAMEWORK;
import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.LabelName.TEST_CLASS;
import static java.util.Objects.nonNull;

/**
 * @author charlie (Dmitry Baev).
 */
public class XunitReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(XunitReader.class);

    public static final String XUNIT_RESULTS_FORMAT = "xunit";

    private static final BigDecimal MULTIPLICAND = new BigDecimal(1000);

    private static final String ASSEMBLIES_ELEMENT_NAME = "assemblies";
    private static final String ASSEMBLY_ELEMENT_NAME = "assembly";
    private static final String COLLECTION_ELEMENT_NAME = "collection";
    private static final String TEST_ELEMENT_NAME = "test";
    private static final String FAILURE_ELEMENT_NAME = "failure";
    private static final String MESSAGE_ELEMENT_NAME = "message";
    private static final String STACK_TRACE_ELEMENT_NAME = "stack-trace";
    private static final String OUTPUT_ELEMENT_NAME = "output";
    private static final String TRAIT_ELEMENT_NAME = "trait";
    private static final String TRAITS_ELEMENT_NAME = "traits";

    private static final String FRAMEWORK_ATTRIBUTE_NAME = "test-framework";
    private static final String METHOD_ATTRIBUTE_NAME = "method";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String RESULT_ATTRIBUTE_NAME = "result";
    private static final String TIME_ATTRIBUTE_NAME = "time";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String VALUE_ATTRIBUTE_NAME = "value";

    @Override
    public void readResults(final ResultsVisitor visitor, final Path file) {
        if (file.getFileName().toString().endsWith(".xml")) {
            parseAssemblies(visitor, file);
        }
    }

    private void parseAssemblies(final ResultsVisitor visitor, final Path parsedFile) {
        try {
            LOGGER.debug("Parsing file {}", parsedFile);
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(parsedFile.toFile());
            final XmlElement assembliesElement = new XmlElement(document.getDocumentElement());
            final String elementName = assembliesElement.getName();
            if (!ASSEMBLIES_ELEMENT_NAME.equals(elementName)) {
                LOGGER.debug("{} is not a valid XUnit xml file. Unknown root element {}", parsedFile, elementName);
                return;
            }

            assembliesElement.get(ASSEMBLY_ELEMENT_NAME)
                    .forEach(element -> parseAssembly(element, visitor));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", parsedFile, e);
        }
    }

    private void parseAssembly(final XmlElement assemblyElement,
                               final ResultsVisitor visitor) {
        final String framework = getFramework(assemblyElement);
        assemblyElement.get(COLLECTION_ELEMENT_NAME)
                .forEach(element -> parseCollection(element, framework, visitor));
    }

    private void parseCollection(final XmlElement collectionElement, final String framework,
                                 final ResultsVisitor visitor) {
        collectionElement.get(TEST_ELEMENT_NAME)
                .forEach(element -> parseTest(element, framework, visitor));
    }

    private void parseTest(final XmlElement testElement, final String framework,
                           final ResultsVisitor visitor) {
        final Optional<String> fullName = Optional.ofNullable(testElement.getAttribute(NAME_ATTRIBUTE_NAME));
        final String className = testElement.getAttribute(TYPE_ATTRIBUTE_NAME);
        final String methodName = testElement.getAttribute(METHOD_ATTRIBUTE_NAME);
        final TestResult result = new TestResult();

        result.setName(methodName);
        result.setStatus(getStatus(testElement));
        result.setDuration(getDuration(testElement));

        fullName.ifPresent(result::setFullName);
        fullName.ifPresent(result::setHistoryId);
        getStatusMessage(testElement).ifPresent(result::setMessage);
        getStatusTrace(testElement).ifPresent(result::setTrace);
        getParameters(testElement).ifPresent(result::setParameters);

        result.addLabelIfNotExists(RESULT_FORMAT, XUNIT_RESULTS_FORMAT);
        if (nonNull(className)) {
            result.addLabelIfNotExists(SUITE, className);
            result.addLabelIfNotExists(TEST_CLASS, className);
            result.addLabelIfNotExists(PACKAGE, className);
        }
        if (nonNull(framework)) {
            result.addLabelIfNotExists(FRAMEWORK, framework);
        }

        visitor.visitTestResult(result);
    }

    private TestStatus getStatus(final XmlElement testElement) {
        final String status = testElement.getAttribute(RESULT_ATTRIBUTE_NAME);
        if ("Pass".equalsIgnoreCase(status)) {
            return TestStatus.PASSED;
        }
        if ("Fail".equalsIgnoreCase(status)) {
            return TestStatus.FAILED;
        }
        if ("Skip".equalsIgnoreCase(status)) {
            return TestStatus.SKIPPED;
        }
        return TestStatus.UNKNOWN;
    }

    private Optional<String> getStatusMessage(final XmlElement testElement) {
        final Optional<String> message = testElement.getFirst(FAILURE_ELEMENT_NAME)
                .flatMap(failure -> failure.getFirst(MESSAGE_ELEMENT_NAME))
                .map(XmlElement::getValue);

        final Optional<String> output = testElement.getFirst(OUTPUT_ELEMENT_NAME)
                .map(XmlElement::getValue);

        if (message.isPresent() && output.isPresent()) {
            return Optional.of(String.format("%s%n%s", message.get(), output.get()));
        }
        if (message.isPresent()) {
            return message;
        }
        return output;
    }

    private Optional<String> getStatusTrace(final XmlElement testElement) {
        return testElement.getFirst(FAILURE_ELEMENT_NAME)
                .flatMap(failure -> failure.getFirst(STACK_TRACE_ELEMENT_NAME))
                .map(XmlElement::getValue);
    }

    private Optional<Set<TestParameter>> getParameters(final XmlElement testElement) {
        return testElement.getFirst(TRAITS_ELEMENT_NAME)
                .map(traits -> traits.get(TRAIT_ELEMENT_NAME))
                .map(Collection::stream)
                .map(stream -> stream.map(this::getParameter))
                .map(stream -> stream.collect(Collectors.toSet()));
    }

    private TestParameter getParameter(final XmlElement traitElement) {
        final String name = traitElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String value = traitElement.getAttribute(VALUE_ATTRIBUTE_NAME);
        return new TestParameter().setName(name).setValue(value);
    }

    private String getFramework(final XmlElement assemblyElement) {
        return assemblyElement.getAttribute(FRAMEWORK_ATTRIBUTE_NAME);
    }

    private Long getDuration(final XmlElement testElement) {
        if (testElement.containsAttribute(TIME_ATTRIBUTE_NAME)) {
            try {
                return BigDecimal.valueOf(testElement.getDoubleAttribute(TIME_ATTRIBUTE_NAME))
                        .multiply(MULTIPLICAND)
                        .longValue();
            } catch (Exception e) {
                LOGGER.debug("Could not parse time attribute for element test", e);
            }
        }
        return null;
    }
}
