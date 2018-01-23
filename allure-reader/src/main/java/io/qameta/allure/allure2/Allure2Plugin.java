package io.qameta.allure.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.AttachmentLink;
import io.qameta.allure.entity.TestLabel;
import io.qameta.allure.entity.TestLink;
import io.qameta.allure.entity.TestParameter;
import io.qameta.allure.entity.TestResultExecution;
import io.qameta.allure.entity.TestResultStep;
import io.qameta.allure.entity.TestStatus;
import io.qameta.allure.model.Allure2ModelJackson;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.ExecutableItem;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static io.qameta.allure.AllureConstants.TEST_RESULT_CONTAINER_FILE_SUFFIX;
import static io.qameta.allure.AllureConstants.TEST_RESULT_FILE_SUFFIX;
import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.util.ConvertUtils.convertList;
import static io.qameta.allure.util.ConvertUtils.convertSet;
import static io.qameta.allure.util.ConvertUtils.firstNonNull;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.nonNull;

/**
 * Plugin that reads results from Allure 2 data format.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class Allure2Plugin implements ResultsReader {

    public static final String ALLURE2_RESULTS_FORMAT = "allure2";

    private final ObjectMapper mapper = Allure2ModelJackson.createMapper();

    @Override
    public void readResultFile(final ResultsVisitor visitor, final Path resultsFile) throws IOException {
        if (resultsFile.getFileName().toString().endsWith(TEST_RESULT_FILE_SUFFIX)) {
            convert(visitor, readTestResult(resultsFile));
        }
        if (resultsFile.getFileName().toString().endsWith(TEST_RESULT_CONTAINER_FILE_SUFFIX)) {
            convert(visitor, readTestResultContainer(resultsFile));
        }
    }

    private void convert(final ResultsVisitor visitor, final TestResultContainer testResultContainer) {
        //TODO support test containers
    }

    private void convert(final ResultsVisitor visitor,
                         final TestResult result) {
        final io.qameta.allure.entity.TestResult dest = new io.qameta.allure.entity.TestResult();
        dest.setHistoryKey(result.getHistoryId());
        dest.setFullName(result.getFullName());
        dest.setName(firstNonNull(result.getName(), result.getFullName(), "Unknown test"));
        dest.setStart(result.getStart());
        dest.setStop(result.getStop());
        dest.setDuration(getDuration(result.getStart(), result.getStop()));
        dest.setDescription(result.getDescription());
        dest.setDescriptionHtml(result.getDescriptionHtml());
        dest.setStatus(convert(result.getStatus()));
        Optional.ofNullable(result.getStatusDetails()).ifPresent(details -> {
            dest.setMessage(details.getMessage());
            dest.setTrace(details.getTrace());
        });

        dest.setLinks(convertSet(result.getLinks(), this::convert));
        dest.setLabels(convertSet(result.getLabels(), this::convert));
        dest.setParameters(getParameters(result));

        dest.addLabelIfNotExists(RESULT_FORMAT, ALLURE2_RESULTS_FORMAT);
        final io.qameta.allure.entity.TestResult stored = visitor.visitTestResult(dest);

        final TestResultExecution execution = getExecution(visitor, stored.getId(), result);
        visitor.visitTestResultExecution(stored.getId(), execution);
    }

    private TestLink convert(final io.qameta.allure.model.Link link) {
        return new TestLink()
                .setName(link.getName())
                .setType(link.getType())
                .setUrl(link.getUrl());
    }

    private TestLabel convert(final io.qameta.allure.model.Label label) {
        return new TestLabel()
                .setName(label.getName())
                .setValue(label.getValue());
    }

    private TestParameter convert(final io.qameta.allure.model.Parameter parameter) {
        return new TestParameter()
                .setName(parameter.getName())
                .setValue(parameter.getValue());
    }

    private AttachmentLink convert(final ResultsVisitor visitor,
                                   final Long testResultId,
                                   final Attachment attachment) {
        final AttachmentLink link = new AttachmentLink()
                .setName(attachment.getName())
                .setContentType(attachment.getType())
                .setFileName(attachment.getSource());

        return visitor.visitAttachmentLink(testResultId, link);
    }

    private TestResultStep convert(final ResultsVisitor visitor,
                                   final Long testResultId,
                                   final StepResult step) {
        final TestResultStep result = new TestResultStep()
                .setName(step.getName())
                .setStatus(convert(step.getStatus()))
                .setStart(step.getStart())
                .setStop(step.getStop())
                .setDuration(getDuration(step.getStart(), step.getStop()))
                .setParameters(convertList(step.getParameters(), this::convert))
                .setAttachments(convertList(step.getAttachments(), attach -> convert(visitor, testResultId, attach)))
                .setSteps(convertList(step.getSteps(), s -> convert(visitor, testResultId, s)));

        Optional.of(step)
                .map(ExecutableItem::getStatusDetails)
                .ifPresent(statusDetails -> {
                    result.setMessage(statusDetails.getMessage());
                    result.setTrace(statusDetails.getTrace());
                });
        return result;
    }

    private TestStatus convert(final io.qameta.allure.model.Status status) {
        if (Objects.isNull(status)) {
            return TestStatus.UNKNOWN;
        }
        return Stream.of(TestStatus.values())
                .filter(item -> item.value().equalsIgnoreCase(status.value()))
                .findAny()
                .orElse(TestStatus.UNKNOWN);
    }


    private Set<TestParameter> getParameters(final TestResult result) {
        final TreeSet<TestParameter> parametersSet = new TreeSet<>(
                comparing(TestParameter::getName, nullsFirst(naturalOrder()))
                        .thenComparing(TestParameter::getValue, nullsFirst(naturalOrder()))
        );
        parametersSet.addAll(convertList(result.getParameters(), this::convert));
        return new HashSet<>(parametersSet);
    }

    private TestResultExecution getExecution(final ResultsVisitor visitor,
                                             final Long testResultId,
                                             final ExecutableItem executableItem) {
        final TestResultExecution execution = new TestResultExecution();
        execution.setSteps(convertList(
                executableItem.getSteps(),
                step -> convert(visitor, testResultId, step)
        ));
        execution.setAttachments(convertList(
                executableItem.getAttachments(),
                attachment -> convert(visitor, testResultId, attachment)
        ));
        return execution;
    }

    private static Long getDuration(final Long start, final Long stop) {
        return nonNull(start) && nonNull(stop) ? stop - start : null;
    }

    private TestResult readTestResult(final Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return mapper.readValue(is, TestResult.class);
        }
    }

    private TestResultContainer readTestResultContainer(final Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return mapper.readValue(is, TestResultContainer.class);
        }
    }
}
