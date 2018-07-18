package io.qameta.allure.allure2;

import io.qameta.allure.FileSystemResultsReader;
import io.qameta.allure.Reader;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.Time;
import io.qameta.allure.model.ExecutableItem;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.nonNull;

/**
 * Plugin that reads results from Allure 2 data format.
 *
 * @since 2.0
 */
@SuppressWarnings({
        "PMD.ExcessiveImports",
        "ClassDataAbstractionCoupling",
        "ClassFanOutComplexity"
})
public class Allure2Plugin implements Reader {

    public static final String ALLURE2_RESULTS_FORMAT = "allure2";

    private static final Comparator<StageResult> BY_START = comparing(
            StageResult::getTime,
            nullsFirst(comparing(Time::getStart, nullsFirst(naturalOrder())))
    );

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path resultsDirectory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        final FileSystemResultsReader reader = new FileSystemResultsReader(resultsDirectory);
        final List<TestResultContainer> groups = reader.readTestResultsContainers().collect(Collectors.toList());
        reader.readTestResults()
                .forEach(result -> convert(context.getValue(), resultsDirectory, visitor, groups, result));
    }

    private void convert(final Supplier<String> uidGenerator,
                         final Path resultsDirectory,
                         final ResultsVisitor visitor,
                         final List<TestResultContainer> groups, final TestResult result) {
        final io.qameta.allure.entity.TestResult dest = new io.qameta.allure.entity.TestResult();
        dest.setUid(uidGenerator.get());
        dest.setHistoryId(result.getHistoryId());
        dest.setFullName(result.getFullName());
        dest.setName(firstNonNull(result.getName(), result.getFullName(), "Unknown test"));
        dest.setTime(Time.create(result.getStart(), result.getStop()));
        dest.setDescription(result.getDescription());
        dest.setDescriptionHtml(result.getDescriptionHtml());
        dest.setStatus(convert(result.getStatus()));
        Optional.ofNullable(result.getStatusDetails()).ifPresent(details -> {
            dest.setStatusMessage(details.getMessage());
            dest.setStatusTrace(details.getTrace());
        });

        dest.setLinks(convert(result.getLinks(), this::convert));
        dest.setLabels(convert(result.getLabels(), this::convert));
        dest.setParameters(getParameters(result));

        dest.addLabelIfNotExists(RESULT_FORMAT, ALLURE2_RESULTS_FORMAT);

        if (hasTestStage(result)) {
            dest.setTestStage(getTestStage(resultsDirectory, visitor, result));
        }

        final List<TestResultContainer> parents = findAllParents(groups, result.getUuid(), new HashSet<>());
        dest.getBeforeStages().addAll(getStages(parents, fixture -> getBefore(resultsDirectory, visitor, fixture)));
        dest.getAfterStages().addAll(getStages(parents, fixture -> getAfter(resultsDirectory, visitor, fixture)));
        visitor.visitTestResult(dest);
    }

    private <T, R> List<R> convert(final List<T> source, final Function<T, R> converter) {
        return Objects.isNull(source) ? Collections.emptyList() : source.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    private StageResult convert(final Path source,
                                final ResultsVisitor visitor,
                                final FixtureResult result) {
        final StageResult stageResult = new StageResult()
                .setName(result.getName())
                .setTime(convert(result.getStart(), result.getStop()))
                .setStatus(convert(result.getStatus()))
                .setSteps(convert(result.getSteps(), step -> convert(source, visitor, step)))
                .setDescription(result.getDescription())
                .setDescriptionHtml(result.getDescriptionHtml())
                .setAttachments(convert(result.getAttachments(), attach -> convert(source, visitor, attach)))
                .setParameters(convert(result.getParameters(), this::convert));
        Optional.of(result)
                .map(ExecutableItem::getStatusDetails)
                .ifPresent(statusDetails -> {
                    stageResult.setStatusMessage(statusDetails.getMessage());
                    stageResult.setStatusTrace(statusDetails.getTrace());
                });

        return stageResult;
    }

    private Link convert(final io.qameta.allure.model.Link link) {
        return new Link()
                .setName(link.getName())
                .setType(link.getType())
                .setUrl(link.getUrl());
    }

    private Label convert(final io.qameta.allure.model.Label label) {
        return new Label()
                .setName(label.getName())
                .setValue(label.getValue());
    }

    private Parameter convert(final io.qameta.allure.model.Parameter parameter) {
        return new Parameter()
                .setName(parameter.getName())
                .setValue(parameter.getValue());
    }

    private Attachment convert(final Path source,
                               final ResultsVisitor visitor,
                               final io.qameta.allure.model.Attachment attachment) {
        final Path attachmentFile = source.resolve(attachment.getSource());
        if (Files.isRegularFile(attachmentFile)) {
            final Attachment found = visitor.visitAttachmentFile(attachmentFile);
            if (nonNull(attachment.getType())) {
                found.setType(attachment.getType());
            }
            if (nonNull(attachment.getName())) {
                found.setName(attachment.getName());
            }
            return found;
        } else {
            visitor.error("Could not find attachment " + attachment.getSource() + " in directory " + source);
            return new Attachment()
                    .setType(attachment.getType())
                    .setName(attachment.getName())
                    .setSize(0L);
        }
    }

    private Step convert(final Path source,
                         final ResultsVisitor visitor,
                         final StepResult step) {
        final Step result = new Step()
                .setName(step.getName())
                .setStatus(convert(step.getStatus()))
                .setTime(convert(step.getStart(), step.getStop()))
                .setParameters(convert(step.getParameters(), this::convert))
                .setAttachments(convert(step.getAttachments(), attachment -> convert(source, visitor, attachment)))
                .setSteps(convert(step.getSteps(), s -> convert(source, visitor, s)));
        Optional.of(step)
                .map(ExecutableItem::getStatusDetails)
                .ifPresent(statusDetails -> {
                    result.setStatusMessage(statusDetails.getMessage());
                    result.setStatusTrace(statusDetails.getTrace());
                });
        return result;
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

    private Time convert(final Long start, final Long stop) {
        return new Time()
                .setStart(start)
                .setStop(stop)
                .setDuration(nonNull(start) && nonNull(stop) ? stop - start : null);
    }

    private List<Parameter> getParameters(final TestResult result) {
        final TreeSet<Parameter> parametersSet = new TreeSet<>(
                comparing(Parameter::getName, nullsFirst(naturalOrder()))
                        .thenComparing(Parameter::getValue, nullsFirst(naturalOrder()))
        );
        parametersSet.addAll(convert(result.getParameters(), this::convert));
        return new ArrayList<>(parametersSet);
    }

    private StageResult getTestStage(final Path source,
                                     final ResultsVisitor visitor,
                                     final TestResult result) {
        final StageResult testStage = new StageResult();
        testStage.setSteps(convert(result.getSteps(), step -> convert(source, visitor, step)));
        testStage.setAttachments(convert(result.getAttachments(), attachment -> convert(source, visitor, attachment)));
        testStage.setStatus(convert(result.getStatus()));
        testStage.setDescription(result.getDescription());
        testStage.setDescriptionHtml(result.getDescriptionHtml());
        Optional.of(result)
                .map(ExecutableItem::getStatusDetails)
                .ifPresent(statusDetails -> {
                    testStage.setStatusMessage(statusDetails.getMessage());
                    testStage.setStatusTrace(statusDetails.getTrace());
                });
        return testStage;
    }

    private boolean hasTestStage(final TestResult result) {
        return !result.getSteps().isEmpty() || !result.getAttachments().isEmpty();
    }

    private List<StageResult> getStages(final List<TestResultContainer> parents,
                                        final Function<TestResultContainer, Stream<StageResult>> getter) {
        return parents.stream()
                .flatMap(getter)
                .collect(Collectors.toList());
    }

    private Stream<StageResult> getBefore(final Path source,
                                          final ResultsVisitor visitor,
                                          final TestResultContainer container) {
        return convert(container.getBefores(), fixture -> convert(source, visitor, fixture)).stream()
                .sorted(BY_START);
    }

    private Stream<StageResult> getAfter(final Path source,
                                         final ResultsVisitor visitor,
                                         final TestResultContainer container) {
        return convert(container.getAfters(), fixture -> convert(source, visitor, fixture)).stream()
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

    @SafeVarargs
    private static <T> T firstNonNull(final T... items) {
        return Stream.of(items)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "firstNonNull method should have at least one non null parameter"
                ));
    }
}
