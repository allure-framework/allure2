package org.allurefw.report.allure1;

import org.allurefw.Label;
import org.allurefw.ModelUtils;
import org.allurefw.Status;
import org.allurefw.report.ReportDataManager;
import org.allurefw.report.ResultsProcessor;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.Parameter;
import org.allurefw.report.entity.Step;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.Time;
import ru.yandex.qatools.allure.BadXmlCharacterFilterReader;
import ru.yandex.qatools.allure.model.DescriptionType;
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
            Label suiteLabel = ModelUtils.createSuiteLabel(testSuite.getName());
            manager.enrichGroup(suiteLabel, "description", "YAHOO");

            for (TestCaseResult testCaseRow : testSuite.getTestCases()) {
                TestCase testCase = convert(testCaseRow, resultDirectory);
                testCase.getLabels().add(suiteLabel);

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
        dest.setStatus(convert(source.getStatus()));

        if (Objects.nonNull(source.getDescription())) {
            if (DescriptionType.HTML.equals(source.getDescription().getType())) {
                dest.setDescriptionHtml(source.getDescription().getValue());
            } else {
                dest.setDescription(source.getDescription().getValue());
            }
        }

        if (Objects.nonNull(source.getFailure())) {
            dest.setFailure(
                    source.getFailure().getMessage(),
                    source.getFailure().getStackTrace()
            );
        }

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
        dest.setLabels(convertLabels(source.getLabels()));
        return dest;
    }

    protected List<Label> convertLabels(List<ru.yandex.qatools.allure.model.Label> labels) {
        return labels.stream()
                .map(label -> new Label().withName(label.getName()).withValue(label.getValue()))
                .collect(Collectors.toList());
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
                .withStatus(convert(s.getStatus()))
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

    protected Status convert(ru.yandex.qatools.allure.model.Status status) {
        try {
            return Status.fromValue(status.value());
        } catch (Exception ignored) {
            //convert skipped to canceled
            return Status.CANCELED;
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
