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
package io.qameta.allure.xunitxml;

import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.Status;
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
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.FRAMEWORK;
import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.LabelName.TEST_CLASS;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Objects.nonNull;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class XunitXmlPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(XunitXmlPlugin.class);

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
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        listResults(directory).forEach(result -> parseAssemblies(result, context, visitor));
    }

    private void parseAssemblies(final Path parsedFile, final RandomUidContext context, final ResultsVisitor visitor) {
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
                    .forEach(element -> parseAssembly(element, context, visitor));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOGGER.error("Could not parse file {}: {}", parsedFile, e);
        }
    }

    private void parseAssembly(final XmlElement assemblyElement,
                               final RandomUidContext context, final ResultsVisitor visitor) {
        final String framework = getFramework(assemblyElement);
        assemblyElement.get(COLLECTION_ELEMENT_NAME)
                .forEach(element -> parseCollection(element, framework, context, visitor));
    }

    private void parseCollection(final XmlElement collectionElement, final String framework,
                                 final RandomUidContext context, final ResultsVisitor visitor) {
        collectionElement.get(TEST_ELEMENT_NAME)
                .forEach(element -> parseTest(element, framework, context, visitor));
    }

    private void parseTest(final XmlElement testElement, final String framework,
                           final RandomUidContext context, final ResultsVisitor visitor) {
        final Optional<String> fullName = Optional.ofNullable(testElement.getAttribute(NAME_ATTRIBUTE_NAME));
        final String className = testElement.getAttribute(TYPE_ATTRIBUTE_NAME);
        final String methodName = testElement.getAttribute(METHOD_ATTRIBUTE_NAME);
        final TestResult result = new TestResult();

        result.setUid(context.getValue().get());
        result.setName(methodName);
        result.setStatus(getStatus(testElement));
        result.setTime(getTime(testElement));

        fullName.ifPresent(result::setFullName);
        fullName.ifPresent(result::setHistoryId);
        getStatusMessage(testElement).ifPresent(result::setStatusMessage);
        getStatusTrace(testElement).ifPresent(result::setStatusTrace);
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

    private Status getStatus(final XmlElement testElement) {
        final String status = testElement.getAttribute(RESULT_ATTRIBUTE_NAME);
        if ("Pass".equalsIgnoreCase(status)) {
            return Status.PASSED;
        }
        if ("Fail".equalsIgnoreCase(status)) {
            return Status.FAILED;
        }
        if ("Skip".equalsIgnoreCase(status)) {
            return Status.SKIPPED;
        }
        return Status.UNKNOWN;
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

    private Optional<List<Parameter>> getParameters(final XmlElement testElement) {
        return testElement.getFirst(TRAITS_ELEMENT_NAME)
                .map(traits -> traits.get(TRAIT_ELEMENT_NAME))
                .map(Collection::stream)
                .map(stream -> stream.map(this::getParameter))
                .map(stream -> stream.collect(Collectors.toList()));
    }

    private Parameter getParameter(final XmlElement traitElement) {
        final String name = traitElement.getAttribute(NAME_ATTRIBUTE_NAME);
        final String value = traitElement.getAttribute(VALUE_ATTRIBUTE_NAME);
        return new Parameter().setName(name).setValue(value);
    }

    private String getFramework(final XmlElement assemblyElement) {
        return assemblyElement.getAttribute(FRAMEWORK_ATTRIBUTE_NAME);
    }

    private Time getTime(final XmlElement testElement) {
        if (testElement.containsAttribute(TIME_ATTRIBUTE_NAME)) {
            try {
                final long duration = BigDecimal.valueOf(testElement.getDoubleAttribute(TIME_ATTRIBUTE_NAME))
                        .multiply(MULTIPLICAND)
                        .longValue();
                return new Time().setDuration(duration);
            } catch (Exception e) {
                LOGGER.debug("Could not parse time attribute for element test", e);
            }
        }
        return new Time();
    }

    private static List<Path> listResults(final Path directory) {
        final List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, "*.xml")) {
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
