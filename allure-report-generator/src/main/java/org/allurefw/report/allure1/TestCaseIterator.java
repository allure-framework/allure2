package org.allurefw.report.allure1;

import org.allurefw.Status;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.Parameter;
import org.allurefw.report.entity.Step;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.Time;
import org.allurefw.report.io.AbstractTestCaseIterator;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.processMarkdown;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class TestCaseIterator extends AbstractTestCaseIterator<TestSuiteResult, TestCaseResult> {

    /**
     * {@inheritDoc}
     */
    public TestCaseIterator(Path[] resultDirectories) {
        super(resultDirectories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterator<TestSuiteResult> createReader(Path... resultDirectories) {
        return new Allure1ResultIterator(resultDirectories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterator<TestCaseResult> extract(TestSuiteResult testSuite) {
        return testSuite.getTestCases().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TestCase convert(TestCaseResult source) {
        TestCase dest = new TestCase();
        dest.setUid(generateUid());
        dest.setName(source.getTitle() != null ? source.getTitle() : source.getName());
        dest.setStatus(convertStatus(source.getStatus()));

        if (source.getDescription() != null) {
            dest.setDescription(source.getDescription().getValue());
            dest.setDescriptionHtml(source.getDescription().getType() == DescriptionType.HTML
                    ? source.getDescription().getValue()
                    : processMarkdown(source.getDescription().getValue())
            );
        }

        if (source.getFailure() != null) {
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
        dest.setSteps(convertSteps(source.getSteps()));
        dest.setAttachments(convertAttachments(source.getAttachments()));
        return dest;
    }

    protected List<Step> convertSteps(
            List<ru.yandex.qatools.allure.model.Step> steps) {
        return steps.stream()
                .map(s -> new Step()
                        .withName(s.getTitle())
                        .withTime(new Time()
                                .withStart(s.getStart())
                                .withStop(s.getStop())
                                .withDuration(s.getStop() - s.getStart()))
                        .withStatus(convertStatus(s.getStatus()))
                        .withSteps(convertSteps(s.getSteps()))
                        .withAttachments(convertAttachments(s.getAttachments())))
                .collect(Collectors.toList());
    }

    protected List<Attachment> convertAttachments(
            List<ru.yandex.qatools.allure.model.Attachment> attachments) {
        return attachments.stream()
                .map(a -> new Attachment()
                        .withName(a.getTitle())
                        .withSource(a.getSource())
                        .withType(a.getType()))
                .collect(Collectors.toList());
    }

    protected Status convertStatus(
            ru.yandex.qatools.allure.model.Status status) {
        try {
            return Status.fromValue(status.value());
        } catch (Exception ignored) {
            //convert skipped to canceled
            return Status.CANCELED;
        }
    }
}
