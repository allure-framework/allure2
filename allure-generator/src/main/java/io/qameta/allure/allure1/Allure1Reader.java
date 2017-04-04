package io.qameta.allure.allure1;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.Time;
import org.allurefw.allure1.AllureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.Failure;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.LabelName.ISSUE;
import static io.qameta.allure.entity.LabelName.PACKAGE;
import static io.qameta.allure.entity.LabelName.PARENT_SUITE;
import static io.qameta.allure.entity.LabelName.SUB_SUITE;
import static io.qameta.allure.entity.LabelName.SUITE;
import static io.qameta.allure.entity.LabelName.TEST_CLASS;
import static io.qameta.allure.entity.LabelName.TEST_ID;
import static io.qameta.allure.entity.LabelName.TEST_METHOD;
import static io.qameta.allure.entity.Status.BROKEN;
import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.SKIPPED;
import static io.qameta.allure.utils.ListUtils.firstNonNull;
import static org.allurefw.allure1.AllureUtils.unmarshalTestSuite;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class Allure1Reader implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure1Reader.class);
    private static final String UNKNOWN = "unknown";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path resultsDirectory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        getStreamOfAllure1Results(resultsDirectory).forEach(testSuite -> testSuite.getTestCases()
                .forEach(testCase -> convert(context.getValue(), resultsDirectory, visitor, testSuite, testCase))
        );
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    private void convert(final Supplier<String> randomUid,
                         final Path directory,
                         final ResultsVisitor visitor,
                         final TestSuiteResult testSuite,
                         final ru.yandex.qatools.allure.model.TestCaseResult source) {
        final TestCaseResult dest = new TestCaseResult();
        final String suiteName = firstNonNull(testSuite.getTitle(), testSuite.getName(), "unknown test suite");
        final String testClass = firstNonNull(
                findLabel(source.getLabels(), TEST_CLASS.value()),
                findLabel(testSuite.getLabels(), TEST_CLASS.value()),
                testSuite.getName(),
                UNKNOWN
        );
        final String testMethod = firstNonNull(
                findLabel(source.getLabels(), TEST_METHOD.value()),
                source.getName(),
                UNKNOWN
        );
        final String name = firstNonNull(source.getTitle(), source.getName(), "unknown test case");

        dest.setTestCaseId(String.format("%s#%s", testClass, name));
        dest.setUid(randomUid.get());
        dest.setName(name);
        dest.setFullName(String.format("%s.%s", testClass, testMethod));

        dest.setStatus(convert(source.getStatus()));
        dest.setTime(source.getStart(), source.getStop());
        dest.setParameters(convert(source.getParameters(), this::hasArgumentType, this::convert));
        dest.setDescription(getDescription(testSuite.getDescription(), source.getDescription()));
        dest.setDescriptionHtml(getDescriptionHtml(testSuite.getDescription(), source.getDescription()));
        dest.setStatusDetails(convert(source.getFailure()));

        if (!source.getSteps().isEmpty() || !source.getAttachments().isEmpty()) {
            StageResult testStage = new StageResult();
            testStage.setSteps(convert(source.getSteps(), step -> convert(directory, visitor, step)));
            testStage.setAttachments(convert(source.getAttachments(), attach -> convert(directory, visitor, attach)));
            testStage.setStatus(convert(source.getStatus()));
            testStage.setStatusDetails(convert(source.getFailure()));
            dest.setTestStage(testStage);
        }

        final Set<Label> set = new TreeSet<>(Comparator.comparing(Label::getName).thenComparing(Label::getValue));
        set.addAll(convert(testSuite.getLabels(), this::convert));
        set.addAll(convert(source.getLabels(), this::convert));
        dest.setLabels(new ArrayList<>(set));
        dest.findOne(ISSUE).ifPresent(issue ->
                dest.getLinks().add(getLink(ISSUE, issue, getIssueUrl(issue)))
        );
        dest.findOne(TEST_ID).ifPresent(testId ->
                dest.getLinks().add(getLink(TEST_ID, testId, getTestCaseIdUrl(testId)))
        );

        //TestNG nested suite
        final Optional<String> testGroupLabel = dest.findOne("testGroup");
        final Optional<String> testSuiteLabel = dest.findOne("testSuite");

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
        dest.findAll("status_details").stream()
                .filter("flaky"::equalsIgnoreCase)
                .findAny()
                .ifPresent(value -> dest.getStatusDetailsSafe().setFlaky(true));
        visitor.visitTestResult(dest);
    }

    private <T, R> List<R> convert(final Collection<T> source, final Function<T, R> converter) {
        return convert(source, t -> true, converter);
    }

    private <T, R> List<R> convert(final Collection<T> source,
                                   final Predicate<T> predicate,
                                   final Function<T, R> converter) {
        return Objects.isNull(source) ? null : source.stream()
                .filter(predicate)
                .map(converter)
                .collect(Collectors.toList());
    }

    private Step convert(final Path source,
                         final ResultsVisitor visitor,
                         final ru.yandex.qatools.allure.model.Step s) {
        return new Step()
                .withName(s.getTitle() == null ? s.getName() : s.getTitle())
                .withTime(new Time()
                        .withStart(s.getStart())
                        .withStop(s.getStop())
                        .withDuration(s.getStop() - s.getStart()))
                .withStatus(convert(s.getStatus()))
                .withSteps(convert(s.getSteps(), step -> convert(source, visitor, step)))
                .withAttachments(convert(s.getAttachments(), attach -> convert(source, visitor, attach)));
    }

    private StatusDetails convert(final Failure failure) {
        return Objects.isNull(failure) ? null : new StatusDetails()
                .withMessage(failure.getMessage())
                .withTrace(failure.getStackTrace());
    }

    private Label convert(final ru.yandex.qatools.allure.model.Label label) {
        return new Label()
                .withName(label.getName())
                .withValue(label.getValue());
    }

    private Parameter convert(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return new Parameter()
                .withName(parameter.getName())
                .withValue(parameter.getValue());
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
                    .withType(attachment.getType())
                    .withName(attachment.getTitle())
                    .withSize(0L);
        }
    }

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

    private String findLabel(final List<ru.yandex.qatools.allure.model.Label> labels, final String labelName) {
        return labels.stream()
                .filter(label -> labelName.equals(label.getName()))
                .map(ru.yandex.qatools.allure.model.Label::getValue)
                .findAny()
                .orElse(null);
    }

    private boolean hasArgumentType(final ru.yandex.qatools.allure.model.Parameter parameter) {
        return ParameterKind.ARGUMENT.equals(parameter.getKind());
    }

    private Link getLink(final LabelName labelName, final String value, final String url) {
        return new Link().withName(value).withType(labelName.value()).withUrl(url);
    }

    private String getIssueUrl(final String issue) {
        return String.format(
                System.getProperty("allure.issues.tracker.pattern", "%s"),
                issue
        );
    }

    private String getTestCaseIdUrl(final String testCaseId) {
        return String.format(
                System.getProperty("allure.tests.management.pattern", "%s"),
                testCaseId
        );
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
        try {
            return Optional.of(unmarshalTestSuite(source));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {}: {}", source, e);
        }
        return Optional.empty();
    }

    private Optional<TestSuiteResult> readJsonTestSuiteFile(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Optional.of(mapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {}: {}", source, e);
            return Optional.empty();
        }
    }

}
