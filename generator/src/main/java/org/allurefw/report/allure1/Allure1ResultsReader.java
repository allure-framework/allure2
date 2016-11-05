package org.allurefw.report.allure1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.baev.BadXmlCharactersFilterReader;
import com.google.inject.Inject;
import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.ReportApiUtils;
import org.allurefw.report.ResultsReader;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.LabelName;
import org.allurefw.report.entity.Parameter;
import org.allurefw.report.entity.Step;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.allurefw.allure1.AllureConstants.ATTACHMENTS_FILE_GLOB;
import static org.allurefw.allure1.AllureConstants.TEST_SUITE_JSON_FILE_GLOB;
import static org.allurefw.allure1.AllureConstants.TEST_SUITE_XML_FILE_GLOB;
import static org.allurefw.report.ReportApiUtils.listFiles;
import static org.allurefw.report.allure1.Allure1ModelConvertUtils.convertDescription;
import static org.allurefw.report.allure1.Allure1ModelConvertUtils.convertFailure;
import static org.allurefw.report.allure1.Allure1ModelConvertUtils.convertLabels;
import static org.allurefw.report.allure1.Allure1ModelConvertUtils.convertList;
import static org.allurefw.report.allure1.Allure1ModelConvertUtils.convertStatus;

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
        String suiteName = Stream.of(testSuite.getTitle(), testSuite.getName())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("unknownSuite");
        String testClass = testSuite.getName();

        TestCaseResult dest = new TestCaseResult();
        String name = source.getTitle() != null ? source.getTitle() : source.getName();

        dest.setId(String.format("%s#%s", testClass, name));
        dest.setUid(ReportApiUtils.generateUid());
        dest.setName(name);
        dest.setStatus(convertStatus(source.getStatus()));

        dest.setTime(source.getStart(), source.getStop());
        source.getParameters().stream()
                .filter(parameter -> ParameterKind.ARGUMENT.equals(parameter.getKind()))
                .forEach(parameter -> dest.getParameters().add(new Parameter()
                        .withName(parameter.getName())
                        .withValue(parameter.getValue())
                ));

        dest.setSteps(convertList(source.getSteps(), this::convert));
        dest.setAttachments(convertList(source.getAttachments(), this::convert));

        convertDescription(dest, source.getDescription());
        convertFailure(dest, source.getFailure());
        convertLabels(dest, source.getLabels());

        testSuite.getLabels().forEach(label -> {
            Optional<String> any = dest.findAll(label.getName()).stream()
                    .filter(value -> value.equals(label.getValue()))
                    .findAny();
            if (!any.isPresent()) {
                dest.addLabel(label.getName(), label.getValue());
            }
        });

        dest.addLabelIfNotExists(LabelName.SUITE, suiteName);
        dest.addLabelIfNotExists(LabelName.TEST_CLASS, testClass);

        return dest;
    }

    protected Step convert(ru.yandex.qatools.allure.model.Step s) {
        return new Step()
                .withName(s.getTitle() == null ? s.getName() : s.getTitle())
                .withTime(new Time()
                        .withStart(s.getStart())
                        .withStop(s.getStop())
                        .withDuration(s.getStop() - s.getStart()))
                .withStatus(convertStatus(s.getStatus()))
                .withSteps(convertList(s.getSteps(), this::convert))
                .withAttachments(convertList(s.getAttachments(), this::convert));
    }

    protected Attachment convert(ru.yandex.qatools.allure.model.Attachment attachment) {
        return storage.findAttachmentByFileName(attachment.getSource())
                .map(result -> {
                    if (attachment.getType() != null) {
                        result.setType(attachment.getType());
                    }
                    return result;
                })
                .map(result -> {
                    if (attachment.getTitle() != null) {
                        result.setName(attachment.getTitle());
                    }
                    return result;
                }).orElseGet(Attachment::new);
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
