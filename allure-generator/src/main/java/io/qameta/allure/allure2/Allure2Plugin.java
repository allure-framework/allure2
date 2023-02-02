/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.allure2;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import java.util.stream.StreamSupport;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static io.qameta.allure.model.Parameter.Mode.HIDDEN;
import static io.qameta.allure.model.Parameter.Mode.MASKED;
import static io.qameta.allure.util.ConvertUtils.convertList;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;
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

    @SuppressWarnings("WeakerAccess")
    public static final String ALLURE2_RESULTS_FORMAT = "allure2";

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2Plugin.class);

    private static final Comparator<StageResult> BY_START = comparing(
            StageResult::getTime,
            nullsLast(comparing(Time::getStart, nullsLast(naturalOrder())))
    );

    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
            .build();

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path resultsDirectory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        final List<TestResultContainer> groups = readTestResultsContainers(resultsDirectory)
                .collect(Collectors.toList());

        readTestResults(resultsDirectory)
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
            dest.setFlaky(details.isFlaky());
        });

        dest.setLinks(convertList(result.getLinks(), this::convert));
        dest.setLabels(convertList(result.getLabels(), this::convert));
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

    private StageResult convert(final Path source,
                                final ResultsVisitor visitor,
                                final FixtureResult result) {
        final StageResult stageResult = new StageResult()
                .setName(result.getName())
                .setTime(convert(result.getStart(), result.getStop()))
                .setStatus(convert(result.getStatus()))
                .setSteps(convertList(result.getSteps(), step -> convert(source, visitor, step)))
                .setDescription(result.getDescription())
                .setDescriptionHtml(result.getDescriptionHtml())
                .setAttachments(convertList(result.getAttachments(), attach -> convert(source, visitor, attach)))
                .setParameters(convertList(result.getParameters(), p -> !HIDDEN.equals(p.getMode()), this::convert));
        Optional.of(result)
                .map(FixtureResult::getStatusDetails)
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
        final boolean masked = MASKED.equals(parameter.getMode());
        return new Parameter()
                .setName(parameter.getName())
                .setValue(masked ? "******" : parameter.getValue());
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
                .setParameters(convertList(step.getParameters(), p -> !HIDDEN.equals(p.getMode()), this::convert))
                .setAttachments(convertList(step.getAttachments(), attachment -> convert(source, visitor, attachment)))
                .setSteps(convertList(step.getSteps(), s -> convert(source, visitor, s)));
        Optional.of(step)
                .map(StepResult::getStatusDetails)
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
        parametersSet.addAll(convertList(result.getParameters(), p -> !HIDDEN.equals(p.getMode()), this::convert));
        return new ArrayList<>(parametersSet);
    }

    private StageResult getTestStage(final Path source,
                                     final ResultsVisitor visitor,
                                     final TestResult result) {
        final StageResult testStage = new StageResult();
        testStage.setSteps(convertList(
                result.getSteps(),
                step -> convert(source, visitor, step)
        ));
        testStage.setAttachments(convertList(
                result.getAttachments(),
                attachment -> convert(source, visitor, attachment)
        ));
        testStage.setStatus(convert(result.getStatus()));
        testStage.setDescription(result.getDescription());
        testStage.setDescriptionHtml(result.getDescriptionHtml());
        Optional.of(result)
                .map(TestResult::getStatusDetails)
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
                .sorted(BY_START)
                .collect(Collectors.toList());
    }

    private Stream<StageResult> getBefore(final Path source,
                                          final ResultsVisitor visitor,
                                          final TestResultContainer container) {
        return convertList(container.getBefores(), fixture -> convert(source, visitor, fixture)).stream();
    }

    private Stream<StageResult> getAfter(final Path source,
                                         final ResultsVisitor visitor,
                                         final TestResultContainer container) {
        return convertList(container.getAfters(), fixture -> convert(source, visitor, fixture)).stream();
    }

    private List<TestResultContainer> findAllParents(final List<TestResultContainer> groups,
                                                     final String id,
                                                     final Set<String> seen) {
        final List<TestResultContainer> parents = findParents(groups, id, seen);
        final List<TestResultContainer> result = new ArrayList<>(parents);
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

    private Stream<TestResultContainer> readTestResultsContainers(final Path resultsDirectory) {
        return listFiles(resultsDirectory, "*-container.json")
                .map(this::readTestResultContainer)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<TestResult> readTestResults(final Path resultsDirectory) {
        return listFiles(resultsDirectory, "*-result.json")
                .map(this::readTestResult)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<TestResult> readTestResult(final Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return Optional.ofNullable(mapper.readValue(is, TestResult.class));
        } catch (IOException e) {
            LOGGER.error("Could not read test result file {}", file, e);
            return Optional.empty();
        }
    }

    private Optional<TestResultContainer> readTestResultContainer(final Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return Optional.ofNullable(mapper.readValue(is, TestResultContainer.class));
        } catch (IOException e) {
            LOGGER.error("Could not read result container file {}", file, e);
            return Optional.empty();
        }
    }

    private Stream<Path> listFiles(final Path directory, final String glob) {
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, glob)) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList())
                    .stream();
        } catch (IOException e) {
            LOGGER.error("Could not list files in directory {}", directory, e);
            return Stream.empty();
        }
    }
}
