package io.qameta.allure.allure1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.TestParameter;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.entity.TestResultStep;
import io.qameta.allure.entity.TestStatus;
import io.qameta.allure.parser.XmlParserModule;
import io.qameta.allure.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.Attachment;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME;
import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.PARENT_SUITE;
import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.entity.LabelName.SUB_SUITE;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.LabelName.TEST_CLASS;
import static io.qameta.allure.entity.LabelName.TEST_METHOD;
import static io.qameta.allure.entity.TestStatus.BROKEN;
import static io.qameta.allure.entity.TestStatus.FAILED;
import static io.qameta.allure.entity.TestStatus.PASSED;
import static io.qameta.allure.entity.TestStatus.SKIPPED;
import static io.qameta.allure.util.ConvertUtils.convertList;
import static io.qameta.allure.util.ConvertUtils.convertSet;
import static io.qameta.allure.util.ConvertUtils.firstNonNull;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Plugin that reads results from Allure1 data format.
 *
 * @since 2.0
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.GodClass", "PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
public class Allure1Reader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure1Reader.class);
    private static final String UNKNOWN = "unknown";

    public static final String ALLURE1_RESULTS_FORMAT = "allure1";

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper xmlMapper;

    public Allure1Reader() {
        final SimpleModule module = new XmlParserModule()
                .addDeserializer(ru.yandex.qatools.allure.model.Status.class, new StatusDeserializer());
        xmlMapper = new XmlMapper()
                .configure(USE_WRAPPER_NAME_AS_PROPERTY_NAME, true)
                .setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
                .registerModule(module);
    }

    @SuppressWarnings("all")
    @Override
    public void readResultFile(final ResultsVisitor visitor,
                               final Path file) {
        if (FileUtils.endsWith(file, "-testsuite.xml")) {
            readXmlTestSuiteFile(file)
                    .ifPresent(testSuite -> convert(visitor, testSuite));
        }
        if (FileUtils.endsWith(file, "-testsuite.json")) {
            readJsonTestSuiteFile(file)
                    .ifPresent(testSuite -> convert(visitor, testSuite));
        }
    }

    private void convert(final ResultsVisitor visitor,
                         final TestSuiteResult testSuite) {
        testSuite.getTestCases()
                .forEach(testCase -> convert(visitor, testSuite, testCase));
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    private void convert(final ResultsVisitor visitor,
                         final TestSuiteResult testSuite,
                         final TestCaseResult source) {
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

        final Set<TestParameter> parameters = getParameters(source);

        findLabel(source.getLabels(), "historyId").ifPresent(dest::setHistoryKey);
        dest.setName(name);
        dest.setFullName(String.format("%s.%s", testClass, testMethod));
        dest.setTestId(String.format("%s#%s", testClass, name));

        final TestStatus status = convert(source.getStatus());
        dest.setStatus(status);
        dest.setStart(source.getStart());
        dest.setStop(source.getStop());
        dest.setDuration(getDuration(source.getStart(), source.getStop()));
        dest.setParameters(parameters);
        dest.setDescription(getDescription(testSuite.getDescription(), source.getDescription()));
        dest.setDescriptionHtml(getDescriptionHtml(testSuite.getDescription(), source.getDescription()));
        Optional.ofNullable(source.getFailure()).ifPresent(failure -> {
            dest.setMessage(failure.getMessage());
            dest.setTrace(failure.getStackTrace());
        });

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

        final TestResult stored = visitor.visitTestResult(dest);

        if (!source.getSteps().isEmpty() || !source.getAttachments().isEmpty()) {
            final Function<Step, TestResultStep> convertStep = step -> convert(stored, visitor, step);
            final List<TestResultStep> steps = convertList(source.getSteps(), convertStep);
            final Function<Attachment, AttachmentLink> convertAttachment = attach -> convert(stored, visitor, attach);
            final List<AttachmentLink> attachments = convertList(source.getAttachments(), convertAttachment);

            final TestResultExecution testResultExecution = new TestResultExecution()
                    .setAttachments(attachments)
                    .setSteps(steps);

            visitor.visitTestResultExecution(stored.getId(), testResultExecution);
        }

//        dest.findAllLabels(ISSUE).forEach(issue ->
//                dest.getLinks().add(getLink(ISSUE, issue, getIssueUrl(issue, properties)))
//        );
//        dest.findOneLabel("testId").ifPresent(testId ->
//                dest.getLinks().add(new TestLink().setName(testId).setType("tms")
//                        .setUrl(getTestCaseIdUrl(testId, properties)))
//        );
    }

    private TestResultStep convert(final TestResult result,
                                   final ResultsVisitor visitor,
                                   final Step step) {
        final TestStatus testStatus = convert(step.getStatus());
        final TestResultStep current = new TestResultStep()
                .setName(step.getTitle() == null ? step.getName() : step.getTitle())
                .setStart(step.getStart())
                .setStop(step.getStop())
                .setDuration(step.getStop() - step.getStart())
                .setStatus(testStatus)
                .setAttachments(convertList(step.getAttachments(), attachment -> convert(result, visitor, attachment)))
                .setSteps(convertList(step.getSteps(), s -> convert(result, visitor, s)));

        //Copy test status details to each step set the same status
        if (Objects.equals(result.getStatus(), testStatus)) {
            current.setMessage(result.getMessage());
            current.setTrace(result.getTrace());
        }
        return current;
    }

    private AttachmentLink convert(final TestResult testResult,
                                   final ResultsVisitor visitor,
                                   final Attachment attachment) {
        final AttachmentLink link = new AttachmentLink()
                .setName(attachment.getTitle())
                .setContentLength(isNull(attachment.getSize()) ? null : Long.valueOf(attachment.getSize()))
                .setFileName(attachment.getSource())
                .setContentType(attachment.getType());
        visitor.visitAttachmentLink(testResult.getId(), link);
        return link;
    }

    private TestParameter convert(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return new TestParameter()
                .setName(parameter.getName())
                .setValue(parameter.getValue());
    }

    public static TestStatus convert(final ru.yandex.qatools.allure.model.Status status) {
        if (isNull(status)) {
            return TestStatus.UNKNOWN;
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
                return TestStatus.UNKNOWN;
        }
    }

    private Set<TestParameter> getParameters(final TestCaseResult source) {
        final TreeSet<TestParameter> parametersSet = new TreeSet<>(
                comparing(TestParameter::getName, nullsFirst(naturalOrder()))
                        .thenComparing(TestParameter::getValue, nullsFirst(naturalOrder()))
        );
        parametersSet.addAll(convertSet(source.getParameters(), this::hasArgumentType, this::convert));
        return new HashSet<>(parametersSet);
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

    private String findLabelValue(final List<Label> labels, final String labelName) {
        return labels.stream()
                .filter(label -> labelName.equals(label.getName()))
                .map(Label::getValue)
                .findAny()
                .orElse(null);
    }

    private Optional<String> findLabel(final List<Label> labels, final String labelName) {
        return labels.stream()
                .filter(label -> labelName.equals(label.getName()))
                .map(Label::getValue)
                .findAny();
    }

    private boolean hasArgumentType(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return isNull(parameter.getKind()) || ParameterKind.ARGUMENT.equals(parameter.getKind());
    }

    private Optional<TestSuiteResult> readXmlTestSuiteFile(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Optional.of(xmlMapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.error("Could not read result {}", source, e);
        }
        return Optional.empty();
    }

    private Optional<TestSuiteResult> readJsonTestSuiteFile(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Optional.of(jsonMapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.error("Could not read result {}", source, e);
            return Optional.empty();
        }
    }

    private static Long getDuration(final Long start, final Long stop) {
        return nonNull(start) && nonNull(stop) ? stop - start : null;
    }
}
