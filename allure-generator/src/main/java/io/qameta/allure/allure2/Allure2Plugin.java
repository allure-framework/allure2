/*
 *  Copyright 2016-2024 Qameta Software Inc
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
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
        "ClassDataAbstractionCoupling",
        "ClassFanOutComplexity",
        "PMD.TooManyMethods",
})
public class Allure2Plugin implements Reader {

    @SuppressWarnings("WeakerAccess")
    public static final String ALLURE2_RESULTS_FORMAT = "allure2";

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2Plugin.class);

    private static final Comparator<StageResult> BY_START = comparing(
            StageResult::getTime,
            nullsLast(comparing(Time::getStart, nullsLast(naturalOrder())))
    );

    private static final Comparator<Parameter> PARAMETER_COMPARATOR =
            comparing(Parameter::getName, nullsFirst(naturalOrder()))
                    .thenComparing(Parameter::getValue, nullsFirst(naturalOrder()));

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

        final Map<String, List<StageResult>> befores = new ConcurrentHashMap<>();
        final Map<String, List<StageResult>> afters = new ConcurrentHashMap<>();

        readTestResultsContainers(resultsDirectory)
                .filter(group -> !Objects.isNull(group.getChildren()))
                .forEach(group -> {
                    processStages(visitor, resultsDirectory, group, befores, group.getBefores());
                    processStages(visitor, resultsDirectory, group, afters, group.getAfters());
                });

        sortByStart(befores);
        sortByStart(afters);

        readTestResults(resultsDirectory)
                .forEach(result -> convert(
                        context.getValue(),
                        resultsDirectory, visitor,
                        result,
                        befores, afters
                ));
    }

    private static void sortByStart(final Map<String, List<StageResult>> befores) {
        befores.keySet().forEach(key -> befores.compute(key, (s, stageResults) -> {
            if (Objects.isNull(stageResults)) {
                return null;
            }
            final List<StageResult> res = new ArrayList<>(stageResults);
            res.sort(BY_START);
            return res;
        }));
    }

    private void processStages(final ResultsVisitor visitor,
                               final Path resultsDirectory,
                               final TestResultContainer group,
                               final Map<String, List<StageResult>> befores,
                               final List<FixtureResult> fixtureResults) {
        if (Objects.isNull(fixtureResults)) {
            return;
        }

        final List<StageResult> stages = fixtureResults.stream()
                .map(fixtureResult -> convert(resultsDirectory, visitor, fixtureResult))
                .collect(Collectors.toList());

        final Set<String> visited = ConcurrentHashMap.newKeySet();

        group.getChildren()
                .parallelStream()
                .filter(Objects::nonNull)
                .filter(visited::add)
                .forEach(child -> befores.compute(child, (s, stageResults) -> {
                    if (Objects.isNull(stageResults)) {
                        return new LinkedList<>(stages);
                    }
                    stageResults.addAll(stages);
                    return stageResults;
                }));
    }

    private void convert(final Supplier<String> uidGenerator,
                         final Path resultsDirectory,
                         final ResultsVisitor visitor,
                         final TestResult result,
                         final Map<String, List<StageResult>> befores,
                         final Map<String, List<StageResult>> afters) {
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

        if (nonNull(result.getUuid())) {
            final List<StageResult> resultBefores = befores.get(result.getUuid());
            if (nonNull(resultBefores)) {
                dest.getBeforeStages().addAll(resultBefores);
            }

            final List<StageResult> resultAfters = afters.get(result.getUuid());
            if (nonNull(resultAfters)) {
                dest.getAfterStages().addAll(resultAfters);
            }
        }
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
        final List<Parameter> parameters = convertList(
                result.getParameters(),
                p -> !HIDDEN.equals(p.getMode()),
                this::convert
        );
        if (Objects.isNull(parameters)) {
            return new ArrayList<>();
        }
        final Set<Parameter> parametersSet = new TreeSet<>(PARAMETER_COMPARATOR);
        parametersSet.addAll(parameters);
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
                .parallel()
                .map(this::readTestResultContainer)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<TestResult> readTestResults(final Path resultsDirectory) {
        return listFiles(resultsDirectory, "*-result.json")
                .parallel()
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
            return StreamSupport.stream(directoryStream.spliterator(), true)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList())
                    .stream();
        } catch (IOException e) {
            LOGGER.error("Could not list files in directory {}", directory, e);
            return Stream.empty();
        }
    }
}
