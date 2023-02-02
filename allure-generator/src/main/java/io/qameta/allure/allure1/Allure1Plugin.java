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
package io.qameta.allure.allure1;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.allurefw.allure1.AllureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.LabelName.ISSUE;
import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.PARENT_SUITE;
import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUB_SUITE;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.LabelName.TEST_CLASS;
import static io.qameta.allure.entity.LabelName.TEST_METHOD;
import static io.qameta.allure.entity.Status.BROKEN;
import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.SKIPPED;
import static io.qameta.allure.util.ConvertUtils.convertList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.toList;

/**
 * Plugin that reads results from Allure1 data format.
 *
 * @since 2.0
 */
@SuppressWarnings({
        "PMD.ExcessiveImports",
        "PMD.GodClass",
        "PMD.TooManyMethods",
        "ClassDataAbstractionCoupling",
        "ClassFanOutComplexity",
        "MultipleStringLiterals"
})
public class Allure1Plugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure1Plugin.class);
    private static final String UNKNOWN = "unknown";
    private static final String MD_5 = "md5";
    private static final String ISSUE_URL_PROPERTY = "allure.issues.tracker.pattern";
    private static final String TMS_LINK_PROPERTY = "allure.tests.management.pattern";
    private static final Comparator<Parameter> PARAMETER_COMPARATOR =
            comparing(Parameter::getName, nullsFirst(naturalOrder()))
                    .thenComparing(Parameter::getValue, nullsFirst(naturalOrder()));

    public static final String ENVIRONMENT_BLOCK_NAME = "environment";
    public static final String ALLURE1_RESULTS_FORMAT = "allure1";

    private final ObjectMapper jsonMapper = JsonMapper.builder()
            .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
            .build();

    private final ObjectMapper xmlMapper = XmlMapper.builder()
            .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
            .annotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
            .addModule(new XmlParserModule())
            .build();

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path resultsDirectory) {
        final Properties allureProperties = loadAllureProperties(resultsDirectory);
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);

        final Map<String, String> environment = processEnvironment(resultsDirectory);
        getStreamOfAllure1Results(resultsDirectory).forEach(testSuite -> testSuite.getTestCases()
                .forEach(testCase -> {
                    convert(context.getValue(), resultsDirectory, visitor, testSuite, testCase, allureProperties);
                    getEnvironmentParameters(testCase).forEach(param ->
                            environment.put(param.getName(), param.getValue())
                    );
                })
        );

        visitor.visitExtra(ENVIRONMENT_BLOCK_NAME, environment);
    }

    private List<ru.yandex.qatools.allure.model.Parameter> getEnvironmentParameters(final TestCaseResult testCase) {
        return testCase.getParameters().stream().filter(this::hasEnvType).collect(toList());
    }

    private Properties loadAllureProperties(final Path resultsDirectory) {
        final Path propertiesFile = resultsDirectory.resolve("allure.properties");
        final Properties properties = new Properties();
        if (Files.exists(propertiesFile)) {
            try (InputStream propFile = Files.newInputStream(propertiesFile)) {
                properties.load(propFile);
            } catch (IOException e) {
                LOGGER.error("Error while reading allure.properties file: {}", e.getMessage());
            }
        }
        properties.putAll(System.getProperties());
        return properties;
    }

    @SuppressWarnings({
            "PMD.ExcessiveMethodLength",
            "JavaNCSS",
            "ExecutableStatementCount",
            "PMD.NcssCount"
    })
    private void convert(final Supplier<String> randomUid,
                         final Path directory,
                         final ResultsVisitor visitor,
                         final TestSuiteResult testSuite,
                         final TestCaseResult source,
                         final Properties properties) {
        final TestResult dest = new TestResult();
        final String suiteName = firstNonNull(testSuite.getTitle(), testSuite.getName(), "unknown test suite");
        final String testClass = firstNonNull(
                findLabelValue(source.getLabels(), TEST_CLASS.value()),
                findLabelValue(testSuite.getLabels(), TEST_CLASS.value()),
                testSuite.getName(),
                UNKNOWN
        );
        final String testMethod = firstNonNull(
                findLabelValue(source.getLabels(), TEST_METHOD.value()),
                source.getName(),
                UNKNOWN
        );
        final String name = firstNonNull(source.getTitle(), source.getName(), "unknown test case");

        final List<Parameter> parameters = getParameters(source);
        final Optional<ru.yandex.qatools.allure.model.Label> historyId = findLabel(source.getLabels(), "historyId");
        if (historyId.isPresent()) {
            dest.setHistoryId(historyId.get().getValue());
        } else {
            dest.setHistoryId(getHistoryId(String.format("%s#%s", testClass, name), parameters));
        }
        dest.setUid(randomUid.get());
        dest.setName(name);
        dest.setFullName(String.format("%s.%s", testClass, testMethod));

        final Status status = convert(source.getStatus());
        dest.setStatus(status);
        dest.setTime(Time.create(source.getStart(), source.getStop()));
        dest.setParameters(parameters);
        dest.setDescription(getDescription(testSuite.getDescription(), source.getDescription()));
        dest.setDescriptionHtml(getDescriptionHtml(testSuite.getDescription(), source.getDescription()));
        Optional.ofNullable(source.getFailure()).ifPresent(failure -> {
            dest.setStatusMessage(failure.getMessage());
            dest.setStatusTrace(failure.getStackTrace());
        });

        if (!source.getSteps().isEmpty() || !source.getAttachments().isEmpty()) {
            final StageResult testStage = new StageResult();
            if (!source.getSteps().isEmpty()) {
                //@formatter:off
                testStage.setSteps(convertList(
                    source.getSteps(),
                    step -> convert(directory, visitor, step, status, dest.getStatusMessage(), dest.getStatusTrace()))
                );
                //@formatter:on
            }
            if (!source.getAttachments().isEmpty()) {
                testStage.setAttachments(convertList(
                        source.getAttachments(),
                        at -> convert(directory, visitor, at)
                ));
            }
            testStage.setStatus(status);
            testStage.setStatusMessage(dest.getStatusMessage());
            testStage.setStatusTrace(dest.getStatusTrace());
            dest.setTestStage(testStage);
        }

        final Set<Label> set = new TreeSet<>(
                comparing(Label::getName, nullsFirst(naturalOrder()))
                        .thenComparing(Label::getValue, nullsFirst(naturalOrder()))
        );
        set.addAll(convertList(testSuite.getLabels(), this::convert));
        set.addAll(convertList(source.getLabels(), this::convert));
        dest.setLabels(new ArrayList<>(set));
        dest.findAllLabels(ISSUE).forEach(issue ->
                dest.getLinks().add(getLink(ISSUE, issue, getIssueUrl(issue, properties)))
        );
        dest.findOneLabel("testId").ifPresent(testId ->
                dest.getLinks().add(new Link().setName(testId).setType("tms")
                        .setUrl(getTestCaseIdUrl(testId, properties)))
        );

        //TestNG nested suite
        final Optional<String> testGroupLabel = dest.findOneLabel("testGroup");
        final Optional<String> testSuiteLabel = dest.findOneLabel("testSuite");

        if (testGroupLabel.isPresent() && testSuiteLabel.isPresent()) {
            dest.addLabelIfNotExists(PARENT_SUITE, testSuiteLabel.get());
            dest.addLabelIfNotExists(SUITE, testGroupLabel.get());
            dest.addLabelIfNotExists(SUB_SUITE, testClass);
        } else {
            dest.addLabelIfNotExists(SUITE, suiteName);
        }

        dest.addLabelIfNotExists(TEST_CLASS, testClass);
        dest.addLabelIfNotExists(TEST_METHOD, testMethod);
        dest.addLabelIfNotExists(PACKAGE, testClass);
        dest.findAllLabels("status_details").stream()
                .filter("flaky"::equalsIgnoreCase)
                .findAny()
                .ifPresent(value -> dest.setFlaky(true));
        dest.addLabelIfNotExists(RESULT_FORMAT, ALLURE1_RESULTS_FORMAT);
        visitor.visitTestResult(dest);
    }

    private Step convert(final Path source,
                         final ResultsVisitor visitor,
                         final ru.yandex.qatools.allure.model.Step s,
                         final Status testStatus,
                         final String message,
                         final String trace) {
        final Status status = convert(s.getStatus());
        final Step current = new Step()
                .setName(s.getTitle() == null ? s.getName() : s.getTitle())
                .setTime(new Time()
                        .setStart(s.getStart())
                        .setStop(s.getStop())
                        .setDuration(s.getStop() - s.getStart()))
                .setStatus(status)
                .setSteps(convertList(
                        s.getSteps(),
                        step -> convert(source, visitor, step, testStatus, message, trace)
                ))
                .setAttachments(convertList(
                        s.getAttachments(),
                        attach -> convert(source, visitor, attach))
                );
        //Copy test status details to each step set the same status
        if (Objects.equals(status, testStatus)) {
            current.setStatusMessage(message);
            current.setStatusMessage(trace);
        }
        return current;
    }

    private Label convert(final ru.yandex.qatools.allure.model.Label label) {
        return new Label()
                .setName(label.getName())
                .setValue(label.getValue());
    }

    private Parameter convert(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return new Parameter()
                .setName(parameter.getName())
                .setValue(parameter.getValue());
    }

    private Attachment convert(final Path source,
                               final ResultsVisitor visitor,
                               final ru.yandex.qatools.allure.model.Attachment attachment) {
        final Path attachmentFile = source.resolve(attachment.getSource());
        if (Files.isRegularFile(attachmentFile)) {
            final Attachment found = visitor.visitAttachmentFile(attachmentFile);
            if (Objects.nonNull(attachment.getType())) {
                found.setType(attachment.getType());
            }
            if (Objects.nonNull(attachment.getTitle())) {
                found.setName(attachment.getTitle());
            }
            return found;
        } else {
            visitor.error("Could not find attachment " + attachment.getSource() + " in directory " + source);
            return new Attachment()
                    .setType(attachment.getType())
                    .setName(attachment.getTitle())
                    .setSize(0L);
        }
    }

    @SuppressWarnings("ReturnCount")
    public static Status convert(final ru.yandex.qatools.allure.model.Status status) {
        if (Objects.isNull(status)) {
            return Status.UNKNOWN;
        }
        switch (status) {
            case FAILED:
                return FAILED;
            case BROKEN:
                return BROKEN;
            case PASSED:
                return PASSED;
            case CANCELED:
            case SKIPPED:
            case PENDING:
                return SKIPPED;
            default:
                return Status.UNKNOWN;
        }
    }

    private List<Parameter> getParameters(final TestCaseResult source) {
        final TreeSet<Parameter> parametersSet = new TreeSet<>(
                comparing(Parameter::getName, nullsFirst(naturalOrder()))
                        .thenComparing(Parameter::getValue, nullsFirst(naturalOrder()))
        );
        parametersSet.addAll(convertList(source.getParameters(), this::hasArgumentType, this::convert));
        return new ArrayList<>(parametersSet);
    }

    private String getDescription(final Description... descriptions) {
        return Stream.of(descriptions)
                .filter(Objects::nonNull)
                .filter(isHtmlDescription().negate())
                .map(Description::getValue)
                .collect(Collectors.joining("\n\n"));
    }

    private String getDescriptionHtml(final Description... descriptions) {
        return Stream.of(descriptions)
                .filter(Objects::nonNull)
                .filter(isHtmlDescription())
                .map(Description::getValue)
                .collect(Collectors.joining("</br>"));
    }

    private Predicate<Description> isHtmlDescription() {
        return description -> DescriptionType.HTML.equals(description.getType());
    }

    private String findLabelValue(final List<ru.yandex.qatools.allure.model.Label> labels, final String labelName) {
        return labels.stream()
                .filter(label -> labelName.equals(label.getName()))
                .map(ru.yandex.qatools.allure.model.Label::getValue)
                .findAny()
                .orElse(null);
    }

    private Optional<ru.yandex.qatools.allure.model.Label> findLabel(
            final List<ru.yandex.qatools.allure.model.Label> labels, final String labelName) {
        return labels.stream()
                .filter(label -> labelName.equals(label.getName()))
                .findAny();
    }

    private boolean hasArgumentType(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return Objects.isNull(parameter.getKind()) || ParameterKind.ARGUMENT.equals(parameter.getKind());
    }

    private boolean hasEnvType(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return ParameterKind.ENVIRONMENT_VARIABLE.equals(parameter.getKind());
    }

    private Link getLink(final LabelName labelName, final String value, final String url) {
        return new Link().setName(value).setType(labelName.value()).setUrl(url);
    }

    private String getIssueUrl(final String issue, final Properties properties) {
        return String.format(properties.getProperty(ISSUE_URL_PROPERTY, "%s"), issue);
    }

    private String getTestCaseIdUrl(final String testCaseId, final Properties properties) {
        return String.format(properties.getProperty(TMS_LINK_PROPERTY, "%s"), testCaseId);
    }

    private Stream<TestSuiteResult> getStreamOfAllure1Results(final Path source) {
        return Stream.concat(xmlFiles(source), jsonFiles(source));
    }

    private Stream<TestSuiteResult> xmlFiles(final Path source) {
        try {
            return AllureUtils.listTestSuiteXmlFiles(source)
                    .stream()
                    .map(this::readXmlTestSuiteFile)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (IOException e) {
            LOGGER.error("Could not list allure1 xml files", e);
            return Stream.empty();
        }
    }

    private Stream<TestSuiteResult> jsonFiles(final Path source) {
        try {
            return AllureUtils.listTestSuiteJsonFiles(source)
                    .stream()
                    .map(this::readJsonTestSuiteFile)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (IOException e) {
            LOGGER.error("Could not list allure1 json files", e);
            return Stream.empty();
        }
    }

    private Optional<TestSuiteResult> readXmlTestSuiteFile(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Optional.of(xmlMapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.error("Could not read xml result {}: {}", source, e);
        }
        return Optional.empty();
    }

    private Optional<TestSuiteResult> readJsonTestSuiteFile(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Optional.of(jsonMapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.error("Could not read json result {}: {}", source, e);
            return Optional.empty();
        }
    }

    @SafeVarargs
    private static <T> T firstNonNull(final T... items) {
        return Stream.of(items)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "firstNonNull method should have at least one non null parameter"
                ));
    }

    private static String getHistoryId(final String name, final List<Parameter> parameters) {
        final MessageDigest digest = getMessageDigest();
        digest.update(name.getBytes(UTF_8));
        parameters.stream()
                .sorted(PARAMETER_COMPARATOR)
                .forEachOrdered(parameter -> {
                    digest.update(Objects.toString(parameter.getName()).getBytes(UTF_8));
                    digest.update(Objects.toString(parameter.getValue()).getBytes(UTF_8));
                });
        final byte[] bytes = digest.digest();
        return new BigInteger(1, bytes).toString(16);
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(MD_5);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find md5 hashing algorithm", e);
        }
    }

    private Map<String, String> processEnvironment(final Path directory) {
        final Map<String, String> environment = processEnvironmentProperties(directory);
        environment.putAll(processEnvironmentXml(directory));
        return environment;
    }

    private Map<String, String> processEnvironmentProperties(final Path directory) {
        final Path envPropsFile = directory.resolve("environment.properties");
        final Map<String, String> items = new LinkedHashMap<>();
        if (Files.exists(envPropsFile)) {
            try (InputStream is = Files.newInputStream(envPropsFile)) {
                new Properties() {
                    @Override
                    public Object put(final Object key, final Object value) {
                        return items.put((String) key, (String) value);
                    }
                }.load(is);
            } catch (IOException e) {
                LOGGER.error("Could not read environments.properties file " + envPropsFile, e);
            }
        }
        return items;
    }

    private Map<String, String> processEnvironmentXml(final Path directory) {
        final Path envXmlFile = directory.resolve("environment.xml");
        final Map<String, String> items = new LinkedHashMap<>();
        if (Files.exists(envXmlFile)) {
            try (InputStream fis = Files.newInputStream(envXmlFile)) {
                xmlMapper.readValue(fis, ru.yandex.qatools.commons.model.Environment.class).getParameter().forEach(p ->
                        items.put(p.getKey(), p.getValue())
                );
            } catch (Exception e) {
                LOGGER.error("Could not read environment.xml file " + envXmlFile.toAbsolutePath(), e);
            }
        }
        return items;
    }
}
