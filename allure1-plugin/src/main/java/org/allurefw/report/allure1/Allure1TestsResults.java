package org.allurefw.report.allure1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.baev.BadXmlCharactersFilterReader;
import org.allurefw.report.InMemoryTestsResults;
import org.allurefw.report.ReportApiUtils;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.EnvironmentItem;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.Parameter;
import org.allurefw.report.entity.Status;
import org.allurefw.report.entity.Step;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;
import org.allurefw.report.entity.Time;
import org.allurefw.report.entity.WithDescription;
import org.allurefw.report.entity.WithFailure;
import org.allurefw.report.entity.WithLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.Failure;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.TestSuiteResult;
import ru.yandex.qatools.commons.model.Environment;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.allurefw.allure1.AllureConstants.ENVIRONMENT_FILE_NAME;
import static org.allurefw.allure1.AllureConstants.TEST_SUITE_JSON_FILE_GLOB;
import static org.allurefw.allure1.AllureConstants.TEST_SUITE_XML_FILE_GLOB;
import static org.allurefw.report.ModelUtils.createLabel;
import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.listFiles;
import static org.allurefw.report.entity.Status.BROKEN;
import static org.allurefw.report.entity.Status.CANCELED;
import static org.allurefw.report.entity.Status.FAILED;
import static org.allurefw.report.entity.Status.PASSED;
import static org.allurefw.report.entity.Status.PENDING;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure1TestsResults extends InMemoryTestsResults {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure1TestsResults.class);

    private final Path resultsDirectory;

    private final ObjectMapper mapper;

    public Allure1TestsResults(Path resultsDirectory) {
        this.resultsDirectory = resultsDirectory;
        this.mapper = new ObjectMapper();
        processResults();
    }

    private void processResults() {
        List<EnvironmentItem> environment = getEnvironmentXml();
        environment.addAll(getEnvironmentProperties());

        getStreamOfAllure1Results()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(testSuite -> {
                    String suiteName = Stream.of(testSuite.getTitle(), testSuite.getName())
                            .filter(Objects::nonNull)
                            .findFirst().orElse("unknownSuite");
                    TestGroup group = new TestGroup()
                            .withName(suiteName)
                            .withType("suite");
                    convertDescription(group, testSuite.getDescription());
                    addTestGroup(group);
                    testSuite.getTestCases().stream()
                            .map(this::convert)
                            .forEach(result -> {
                                result.getEnvironment().addAll(environment);
                                result.addLabelIfNotExists(LabelName.SUITE, suiteName);
                                result.addLabelIfNotExists(LabelName.TEST_CLASS, testSuite.getName());
                                addTestCaseResult(result);
                            });
                });
    }

    protected List<EnvironmentItem> getEnvironmentXml() {
        return listFiles(resultsDirectory, "*environment.xml").stream()
                .findAny()
                .flatMap(this::readEnvironmentXml)
                .map(Environment::getParameter)
                .orElse(Collections.emptyList()).stream()
                .map(parameter -> new EnvironmentItem()
                        .withName(parameter.getKey())
                        .withValue(parameter.getValue()))
                .collect(Collectors.toList());
    }

    protected List<EnvironmentItem> getEnvironmentProperties() {
        return Optional.of(resultsDirectory.resolve(ENVIRONMENT_FILE_NAME))
                .filter(Files::exists)
                .filter(Files::isRegularFile)
                .map(this::readEnvironmentProperties)
                .orElse(Collections.emptyList());
    }

    protected Stream<Optional<TestSuiteResult>> getStreamOfAllure1Results() {
        return Stream.concat(
                listFiles(resultsDirectory, TEST_SUITE_XML_FILE_GLOB).stream().map(this::readXmlTestSuiteFile),
                listFiles(resultsDirectory, TEST_SUITE_JSON_FILE_GLOB).stream().map(this::readJsonTestSuiteFile)
        );
    }

    protected TestCaseResult convert(ru.yandex.qatools.allure.model.TestCaseResult source) {
        TestCaseResult dest = new TestCaseResult();
        dest.setUid(generateUid());
        dest.setName(source.getTitle() != null ? source.getTitle() : source.getName());
        dest.setStatus(convertStatus(source.getStatus()));

        dest.setTime(source.getStart(), source.getStop());
        dest.setParameters(source.getParameters().stream()
                .filter(parameter -> ParameterKind.ARGUMENT.equals(parameter.getKind()))
                .map(parameter -> new Parameter()
                        .withName(parameter.getName())
                        .withValue(parameter.getValue()))
                .collect(Collectors.toList())
        );
        dest.setSteps(convert(source.getSteps(), this::convert));
        dest.setAttachments(convert(source.getAttachments(), this::convert));

        convertDescription(dest, source.getDescription());
        convertFailure(dest, source.getFailure());
        convertLabels(dest, source.getLabels());

        return dest;
    }

    protected void convertDescription(WithDescription dest, Description source) {
        if (Objects.nonNull(source)) {
            if (DescriptionType.HTML.equals(source.getType())) {
                dest.setDescriptionHtml(source.getValue());
            } else {
                dest.setDescription(source.getValue());
            }
        }
    }

    protected void convertFailure(WithFailure dest, Failure source) {
        if (Objects.nonNull(source)) {
            dest.setFailure(
                    source.getMessage(),
                    source.getStackTrace()
            );
        }
    }

    protected void convertLabels(WithLabels dest, List<Label> labels) {
        if (Objects.nonNull(labels)) {
            dest.setLabels(labels.stream()
                    .map(label -> createLabel(label.getName(), label.getValue()))
                    .collect(Collectors.toList())
            );
        }
    }

    protected Step convert(ru.yandex.qatools.allure.model.Step s) {
        return new Step()
                .withName(s.getTitle() == null ? s.getName() : s.getTitle())
                .withTime(new Time()
                        .withStart(s.getStart())
                        .withStop(s.getStop())
                        .withDuration(s.getStop() - s.getStart()))
                .withStatus(convertStatus(s.getStatus()))
                .withSteps(convert(s.getSteps(), this::convert))
                .withAttachments(convert(s.getAttachments(), this::convert));
    }


    protected Attachment convert(ru.yandex.qatools.allure.model.Attachment attachment) {
        Path attachmentPath = resultsDirectory.resolve(attachment.getSource());
        Attachment attach = ReportApiUtils.createAttachment(attachmentPath, attachment.getType());
        attach.withName(attachment.getTitle());
        addAttachment(attachmentPath, attach);
        return attach;
    }

    protected <T, R> List<R> convert(List<T> t, Function<T, R> convertFunction) {
        return t.stream()
                .map(convertFunction)
                .collect(Collectors.toList());
    }

    protected Status convertStatus(ru.yandex.qatools.allure.model.Status status) {
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

    protected List<EnvironmentItem> readEnvironmentProperties(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            Properties properties = new Properties();
            properties.load(is);
            return properties.stringPropertyNames().stream()
                    .map(key -> new EnvironmentItem().withName(key).withValue(properties.getProperty(key)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Could not read environment file {}: {}", path, e);
            return Collections.emptyList();
        }
    }

    protected Optional<Environment> readEnvironmentXml(Path path) {
        try (BadXmlCharactersFilterReader reader = new BadXmlCharactersFilterReader(path)) {
            return Optional.of(JAXB.unmarshal(reader, Environment.class));
        } catch (IOException e) {
            LOGGER.error("Could not read environment file {}: {}", path, e);
            return Optional.empty();
        }
    }

    protected Optional<TestSuiteResult> readXmlTestSuiteFile(Path path) {
        try (BadXmlCharactersFilterReader reader = new BadXmlCharactersFilterReader(path)) {
            return Optional.of(JAXB.unmarshal(reader, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.error("Could not read {} file as xml TestSuiteResult: {}", path, e);
            return Optional.empty();
        }
    }

    protected Optional<TestSuiteResult> readJsonTestSuiteFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return Optional.of(mapper.readValue(is, TestSuiteResult.class));
        } catch (IOException e) {
            LOGGER.error("Could not read {} file as json TestSuiteResult: {}", path, e);
            return Optional.empty();
        }
    }
}
