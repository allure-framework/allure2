package io.qameta.allure.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.model.Allure2ModelJackson;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.AllureConstants.TEST_RESULT_CONTAINER_FILE_GLOB;
import static io.qameta.allure.AllureConstants.TEST_RESULT_FILE_GLOB;
import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.ReportApiUtils.listFiles;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.nonNull;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class Allure2ResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2ResultsReader.class);
    private static final Comparator<StageResult> BY_START = comparingLong(a -> a.getTime().getStart());

    private final AttachmentsStorage storage;

    private final ObjectMapper mapper;

    @Inject
    public Allure2ResultsReader(final AttachmentsStorage storage) {
        this.storage = storage;
        this.mapper = Allure2ModelJackson.createMapper();
    }

    @Override
    public List<TestCaseResult> readResults(final Path source) {
        listFiles(source, "*-attachment*")
                .forEach(storage::addAttachment);

        final List<TestResultContainer> groups = listFiles(source, TEST_RESULT_CONTAINER_FILE_GLOB)
                .flatMap(this::readTestResultContainer)
                .collect(Collectors.toList());

        return listFiles(source, TEST_RESULT_FILE_GLOB)
                .flatMap(this::readTestResult)
                .map(result -> convert(groups, result))
                .collect(Collectors.toList());
    }

    private TestCaseResult convert(final List<TestResultContainer> groups, final TestResult result) {
        final TestCaseResult dest = new TestCaseResult();
        dest.setUid(generateUid());

        dest.setTestCaseId(result.getHistoryId());
        dest.setFullName(result.getFullName());
        dest.setName(result.getName());
        dest.setTime(result.getStart(), result.getStop());
        dest.setDescription(result.getDescription());
        dest.setDescriptionHtml(result.getDescriptionHtml());
        dest.setStatus(convert(result.getStatus()));
        dest.setStatusDetails(convert(result.getStatusDetails()));

        dest.setLinks(convert(result.getLinks(), this::convert));
        dest.setLabels(convert(result.getLabels(), this::convert));
        dest.setParameters(convert(result.getParameters(), this::convert));

        if (hasTestStage(result)) {
            dest.setTestStage(getTestStage(result));
        }

        final List<TestResultContainer> parents = findAllParents(groups, result.getUuid(), new HashSet<>());
        dest.getBeforeStages().addAll(getStages(parents, this::getBefore));
        dest.getAfterStages().addAll(getStages(parents, this::getAfter));
        return dest;
    }

    private <T, R> List<R> convert(final List<T> source, final Function<T, R> converter) {
        return Objects.isNull(source) ? Collections.emptyList() : source.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    private StageResult convert(final FixtureResult result) {
        return new StageResult()
                .withName(result.getName())
                .withTime(convert(result.getStart(), result.getStop()))
                .withStatus(convert(result.getStatus()))
                .withStatusDetails(convert(result.getStatusDetails()))
                .withSteps(convert(result.getSteps(), this::convert))
                .withAttachments(convert(result.getAttachments(), this::convert))
                .withParameters(convert(result.getParameters(), this::convert));
    }

    private Link convert(final io.qameta.allure.model.Link link) {
        return new Link()
                .withName(link.getName())
                .withType(link.getType())
                .withUrl(link.getUrl());
    }

    private Label convert(final io.qameta.allure.model.Label label) {
        return new Label()
                .withName(label.getName())
                .withValue(label.getValue());
    }

    private Parameter convert(final io.qameta.allure.model.Parameter parameter) {
        return new Parameter()
                .withName(parameter.getName())
                .withValue(parameter.getValue());
    }

    private Attachment convert(final io.qameta.allure.model.Attachment attachment) {
        final Attachment found = storage.findAttachmentByFileName(attachment.getSource())
                .map(attach -> new Attachment()
                        .withUid(attach.getUid())
                        .withName(attach.getName())
                        .withType(attach.getType())
                        .withSource(attach.getSource())
                        .withSize(attach.getSize()))
                .orElseGet(() -> new Attachment().withName("unknown").withSize(0L).withType("*/*"));

        if (isNotEmpty(attachment.getType())) {
            found.setType(attachment.getType());
        }
        if (isNotEmpty(attachment.getName())) {
            found.setName(attachment.getName());
        }
        return found;
    }

    private Step convert(final StepResult step) {
        return new Step()
                .withName(step.getName())
                .withStatus(convert(step.getStatus()))
                .withStatusDetails(convert(step.getStatusDetails()))
                .withTime(convert(step.getStart(), step.getStop()))
                .withParameters(convert(step.getParameters(), this::convert))
                .withAttachments(convert(step.getAttachments(), this::convert))
                .withSteps(convert(step.getSteps(), this::convert));
    }

    private Status convert(final io.qameta.allure.model.Status status) {
        if (Objects.isNull(status)) {
            return Status.UNKNOWN;
        }
        return Stream.of(Status.values())
                .filter(item -> item.value().equalsIgnoreCase(status.value()))
                .findAny()
                .orElse(Status.UNKNOWN);
    }

    private StatusDetails convert(final io.qameta.allure.model.StatusDetails details) {
        return Objects.isNull(details) ? null : new StatusDetails()
                .withFlaky(details.isFlaky())
                .withMessage(details.getMessage())
                .withTrace(details.getTrace());
    }

    private Time convert(final Long start, final Long stop) {
        return new Time()
                .withStart(start)
                .withStop(stop)
                .withDuration(nonNull(start) && nonNull(stop) ? stop - start : null);
    }

    private StageResult getTestStage(final TestResult result) {
        StageResult testStage = new StageResult();
        testStage.setSteps(convert(result.getSteps(), this::convert));
        testStage.setAttachments(convert(result.getAttachments(), this::convert));
        testStage.setStatus(convert(result.getStatus()));
        testStage.setStatusDetails(convert(result.getStatusDetails()));
        return testStage;
    }

    private boolean hasTestStage(final TestResult result) {
        return !result.getSteps().isEmpty() || !result.getAttachments().isEmpty();
    }

    private List<StageResult> getStages(final List<TestResultContainer> parents,
                                        final Function<TestResultContainer, Stream<StageResult>> getter) {
        return parents.stream().flatMap(getter).collect(Collectors.toList());
    }

    private Stream<StageResult> getBefore(final TestResultContainer container) {
        return convert(container.getBefores(), this::convert).stream()
                .sorted(BY_START);
    }

    private Stream<StageResult> getAfter(final TestResultContainer container) {
        return convert(container.getAfters(), this::convert).stream()
                .sorted(BY_START);
    }

    private List<TestResultContainer> findAllParents(final List<TestResultContainer> groups,
                                                     final String id,
                                                     final Set<String> seen) {
        final List<TestResultContainer> result = new ArrayList<>();
        final List<TestResultContainer> parents = findParents(groups, id, seen);
        result.addAll(parents);
        for (TestResultContainer container : parents) {
            result.addAll(findAllParents(groups, container.getUuid(), seen));
        }
        return result;
    }

    private List<TestResultContainer> findParents(final List<TestResultContainer> groups,
                                                  final String id,
                                                  final Set<String> seen) {
        return groups.stream()
                .filter(container -> container.getChildren().contains(id))
                .filter(container -> !seen.contains(container.getUuid()))
                .collect(Collectors.toList());
    }

    private Stream<TestResult> readTestResult(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Stream.of(mapper.readValue(is, TestResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read test result {}: {}", source, e);
            return Stream.empty();
        }
    }

    private Stream<TestResultContainer> readTestResultContainer(final Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Stream.of(mapper.readValue(is, TestResultContainer.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read test result container {}: {}", source, e);
            return Stream.empty();
        }
    }

    private boolean isNotEmpty(final String s) {
        return nonNull(s) && !s.isEmpty();
    }
}
