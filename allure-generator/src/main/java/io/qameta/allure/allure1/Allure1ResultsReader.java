package io.qameta.allure.allure1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.baev.BadXmlCharactersFilterReader;
import com.google.inject.Inject;
import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Failure;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.LabelName.ISSUE;
import static io.qameta.allure.entity.LabelName.TEST_ID;
import static org.allurefw.allure1.AllureConstants.ATTACHMENTS_FILE_GLOB;
import static org.allurefw.allure1.AllureConstants.TEST_SUITE_JSON_FILE_GLOB;
import static org.allurefw.allure1.AllureConstants.TEST_SUITE_XML_FILE_GLOB;
import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.ReportApiUtils.listFiles;
import static io.qameta.allure.entity.Status.BROKEN;
import static io.qameta.allure.entity.Status.CANCELED;
import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.entity.Status.PENDING;
import static io.qameta.allure.utils.ListUtils.firstNonNull;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure1ResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure1ResultsReader.class);

    private final AttachmentsStorage storage;

    private final ObjectMapper mapper;

    @Inject
    public Allure1ResultsReader(AttachmentsStorage storage) {
        this.storage = storage;
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<TestCaseResult> readResults(Path source) {
        listFiles(source, ATTACHMENTS_FILE_GLOB)
                .forEach(storage::addAttachment);

        return getStreamOfAllure1Results(source)
                .flatMap(testSuite -> testSuite.getTestCases().stream()
                        .map(testCase -> convert(testSuite, testCase)))
                .collect(Collectors.toList());
    }

    private TestCaseResult convert(TestSuiteResult testSuite,
                                   ru.yandex.qatools.allure.model.TestCaseResult source) {
        TestCaseResult dest = new TestCaseResult();


        String suiteName = firstNonNull(testSuite.getTitle(), testSuite.getName(), "unknown test suite");
        String testClass = firstNonNull(testSuite.getName(), "unknown");
        String name = firstNonNull(source.getTitle(), source.getName(), "unknown test case");

        dest.setTestCaseId(String.format("%s#%s", testClass, name));
        dest.setUid(generateUid());
        dest.setName(name);
        dest.setFullName(String.format("%s#%s", testSuite.getName(), source.getName()));

        dest.setStatus(convert(source.getStatus()));
        dest.setTime(source.getStart(), source.getStop());
        dest.setParameters(convert(source.getParameters(), this::hasArgumentType, this::convert));
        dest.setDescription(getDescription(testSuite.getDescription(), source.getDescription()));
        dest.setDescriptionHtml(getDescriptionHtml(testSuite.getDescription(), source.getDescription()));
        dest.setFailure(convert(source.getFailure()));

        if (!source.getSteps().isEmpty() || !source.getAttachments().isEmpty()) {
            StageResult testStage = new StageResult();
            testStage.setSteps(convert(source.getSteps(), this::convert));
            testStage.setAttachments(convert(source.getAttachments(), this::convert));
            testStage.setFailure(convert(source.getFailure()));
            dest.setTestStage(testStage);
        }

        Set<Label> set = new TreeSet<>(Comparator.comparing(Label::getName).thenComparing(Label::getValue));
        set.addAll(convert(testSuite.getLabels(), this::convert));
        set.addAll(convert(source.getLabels(), this::convert));
        dest.setLabels(set.stream().collect(Collectors.toList()));
        dest.findOne(ISSUE).ifPresent(issue ->
                dest.getLinks().add(getLink(ISSUE, issue, getIssueUrl(issue)))
        );
        dest.findOne(TEST_ID).ifPresent(testId ->
                dest.getLinks().add(getLink(TEST_ID, testId, getTestCaseIdUrl(testId)))
        );
        dest.addLabelIfNotExists(LabelName.SUITE, suiteName);
        dest.addLabelIfNotExists("package", testClass);

        return dest;
    }

    private String getDescription(Description... descriptions) {
        return Stream.of(descriptions)
                .filter(Objects::nonNull)
                .filter(isHtmlDescription().negate())
                .map(Description::getValue)
                .collect(Collectors.joining("\n\n"));
    }

    private String getDescriptionHtml(Description... descriptions) {
        return Stream.of(descriptions)
                .filter(Objects::nonNull)
                .filter(isHtmlDescription())
                .map(Description::getValue)
                .collect(Collectors.joining("</br>"));
    }

    private Predicate<Description> isHtmlDescription() {
        return description -> DescriptionType.HTML.equals(description.getType());
    }

    private <T, R> List<R> convert(Collection<T> source, Function<T, R> converter) {
        return convert(source, t -> true, converter);
    }

    private <T, R> List<R> convert(Collection<T> source, Predicate<T> predicate, Function<T, R> converter) {
        return Objects.isNull(source) ? null : source.stream()
                .filter(predicate)
                .map(converter)
                .collect(Collectors.toList());
    }

    private Step convert(ru.yandex.qatools.allure.model.Step s) {
        return new Step()
                .withName(s.getTitle() == null ? s.getName() : s.getTitle())
                .withTime(new Time()
                        .withStart(s.getStart())
                        .withStop(s.getStop())
                        .withDuration(s.getStop() - s.getStart()))
                .withStatus(convert(s.getStatus()))
                .withSteps(convert(s.getSteps(), this::convert))
                .withAttachments(convert(s.getAttachments(), this::convert));
    }

    private Failure convert(ru.yandex.qatools.allure.model.Failure failure) {
        return Objects.isNull(failure) ? null : new Failure()
                .withMessage(failure.getMessage())
                .withTrace(failure.getStackTrace());
    }

    private Label convert(ru.yandex.qatools.allure.model.Label label) {
        return new Label()
                .withName(label.getName())
                .withValue(label.getValue());
    }

    private Parameter convert(ru.yandex.qatools.allure.model.Parameter parameter) {
        return new Parameter()
                .withName(parameter.getName())
                .withValue(parameter.getValue());
    }

    private Attachment convert(ru.yandex.qatools.allure.model.Attachment attachment) {
        Attachment found = storage.findAttachmentByFileName(attachment.getSource())
                .map(attach -> new Attachment()
                        .withUid(attach.getUid())
                        .withName(attach.getName())
                        .withType(attach.getType())
                        .withSource(attach.getSource())
                        .withSize(attach.getSize()))
                .orElseGet(() -> new Attachment().withName("unknown").withSize(0L).withType("*/*"));

        if (Objects.nonNull(attachment.getType())) {
            found.setType(attachment.getType());
        }
        if (Objects.nonNull(attachment.getTitle())) {
            found.setName(attachment.getTitle());
        }
        return found;
    }

    public static Status convert(ru.yandex.qatools.allure.model.Status status) {
        switch (status) {
            case FAILED:
                return FAILED;
            case BROKEN:
                return BROKEN;
            case PASSED:
                return PASSED;
            case CANCELED:
            case SKIPPED:
                return CANCELED;
            default:
                return PENDING;
        }
    }

    private boolean hasArgumentType(ru.yandex.qatools.allure.model.Parameter parameter) {
        return ParameterKind.ARGUMENT.equals(parameter.getKind());
    }

    private Link getLink(LabelName labelName, String value, String url) {
        return new Link().withName(value).withType(labelName.value()).withUrl(url);
    }

    private String getIssueUrl(String issue) {
        return String.format(
                System.getProperty("allure.issues.tracker.pattern", "%s"),
                issue
        );
    }

    private String getTestCaseIdUrl(String testCaseId) {
        return String.format(
                System.getProperty("allure.tests.management.pattern", "%s"),
                testCaseId
        );
    }

    private Stream<TestSuiteResult> getStreamOfAllure1Results(Path source) {
        return Stream.concat(
                listFiles(source, TEST_SUITE_XML_FILE_GLOB).map(this::readXmlTestSuiteFile),
                listFiles(source, TEST_SUITE_JSON_FILE_GLOB).map(this::readJsonTestSuiteFile)
        ).filter(Optional::isPresent).map(Optional::get);
    }

    private Optional<TestSuiteResult> readXmlTestSuiteFile(Path source) {
        try (BadXmlCharactersFilterReader reader = new BadXmlCharactersFilterReader(source)) {
            return Optional.of(JAXB.unmarshal(reader, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {}: {}", source, e);
        }
        return Optional.empty();
    }

    private Optional<TestSuiteResult> readJsonTestSuiteFile(Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Optional.of(mapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read result {}: {}", source, e);
            return Optional.empty();
        }
    }
}
