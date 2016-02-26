package org.allurefw.report.allure1;

import org.allurefw.LabelName;
import org.allurefw.Status;
import org.allurefw.report.ReportDataManager;
import org.allurefw.report.ResultsProcessor;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.GroupInfo;
import org.allurefw.report.entity.Parameter;
import org.allurefw.report.entity.Step;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.Time;
import org.allurefw.report.entity.WithDescription;
import org.allurefw.report.entity.WithFailure;
import org.allurefw.report.entity.WithLabels;
import ru.yandex.qatools.allure.BadXmlCharacterFilterReader;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.Failure;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.allurefw.ModelUtils.createLabel;
import static org.allurefw.ModelUtils.createSuiteLabel;
import static org.allurefw.Status.BROKEN;
import static org.allurefw.Status.CANCELED;
import static org.allurefw.Status.FAILED;
import static org.allurefw.Status.PASSED;
import static org.allurefw.Status.PENDING;
import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.listFilesSafe;
import static ru.yandex.qatools.allure.AllureConstants.TEST_SUITE_XML_FILE_GLOB;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 14.02.16
 */
public class Allure1Results implements ResultsProcessor {

    private ReportDataManager manager;

    @Override
    public void process(Path resultDirectory) {
        List<Path> results = listFilesSafe(TEST_SUITE_XML_FILE_GLOB, resultDirectory);

        for (Path result : results) {
            Optional<TestSuiteResult> unmarshal = unmarshal(result);
            if (!unmarshal.isPresent()) {
                continue;
            }

            TestSuiteResult testSuite = unmarshal.get();

            GroupInfo group = new GroupInfo().withName(testSuite.getName());
            convertDescription(group, testSuite.getDescription());
            manager.addGroupInfo(LabelName.SUITE, group);

            for (TestCaseResult testCaseRow : testSuite.getTestCases()) {
                TestCase testCase = convert(testCaseRow, resultDirectory);
                testCase.getLabels().add(createSuiteLabel(testSuite.getName()));

                manager.addTestCase(testCase);
            }
        }
    }

    @Override
    public void setReportDataManager(ReportDataManager manager) {
        this.manager = manager;
    }

    protected TestCase convert(TestCaseResult source, Path resultDirectory) {
        TestCase dest = new TestCase();
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
        dest.setSteps(convertSteps(resultDirectory, source.getSteps()));
        dest.setAttachments(convertAttachments(resultDirectory, source.getAttachments()));

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

    protected List<Step> convertSteps(
            Path resultDirectory,
            List<ru.yandex.qatools.allure.model.Step> steps) {
        return steps.stream()
                .map(step -> convert(resultDirectory, step))
                .collect(Collectors.toList());
    }

    protected Step convert(Path resultDirectory,
                           ru.yandex.qatools.allure.model.Step s) {
        return new Step()
                .withName(s.getTitle())
                .withTime(new Time()
                        .withStart(s.getStart())
                        .withStop(s.getStop())
                        .withDuration(s.getStop() - s.getStart()))
                .withStatus(convertStatus(s.getStatus()))
                .withSteps(convertSteps(resultDirectory, s.getSteps()))
                .withAttachments(convertAttachments(resultDirectory, s.getAttachments()));
    }

    protected List<Attachment> convertAttachments(Path resultDirectory,
                                                  List<ru.yandex.qatools.allure.model.Attachment> attachments) {
        return attachments.stream()
                .map(a -> convert(resultDirectory, a))
                .collect(Collectors.toList());
    }

    protected Attachment convert(Path resultDirectory,
                                 ru.yandex.qatools.allure.model.Attachment attachment) {
        return manager.addAttachment(
                resultDirectory.resolve(attachment.getSource()),
                attachment.getType()
        ).withName(attachment.getTitle());
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

    protected Optional<TestSuiteResult> unmarshal(Path path) {
        try (BadXmlCharacterFilterReader reader = new BadXmlCharacterFilterReader(path)) {
            return Optional.of(JAXB.unmarshal(reader, TestSuiteResult.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
