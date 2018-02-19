package io.qameta.allure.allure3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.ReadError;
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
import io.qameta.allure.model3.StepResult;
import io.qameta.allure.model3.TestResult;
import io.qameta.allure.model3.TestResultType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
import java.util.stream.StreamSupport;

import static io.qameta.allure.entity.LabelName.RESULT_FORMAT;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.nonNull;

/**
 * Plugin that reads results from Allure 3 data format.
 *
 * @since 3.0
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class Allure3Plugin implements Reader {

    public static final String ALLURE3_RESULTS_FORMAT = "allure3";

    public static final String TEST_RESULT_FILE_SUFFIX = "-allure.json";

    private static final String TEST_RESULT_FILE_GLOB = "*-allure.json";

    private final ObjectMapper mapper;

    private final List<ReadError> errors = new ArrayList<>();

    private static final Comparator<StageResult> BY_START = comparing(
            StageResult::getTime,
            nullsFirst(comparing(Time::getStart, nullsFirst(naturalOrder())))
    );

    public Allure3Plugin() {
        this.mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path resultsDirectory) {
        final RandomUidContext context = configuration.requireContext(RandomUidContext.class);
        List<TestResult> allResults = readTestResults(resultsDirectory).collect(Collectors.toList());
        List<TestResult> fixtures = allResults.stream()
                .filter(result -> TestResultType.SET_UP.equals(result.getType())
                        || TestResultType.TEAR_DOWN.equals(result.getType())).collect(Collectors.toList());
        allResults.stream().filter(result -> TestResultType.TEST.equals(result.getType())).forEach(
            result -> convert(context.getValue(), resultsDirectory, visitor, fixtures, result)
        );
    }

    private Stream<TestResult> readTestResults(final Path resultsDirectory) {
        return listFiles(resultsDirectory)
                .map(this::readTestResult)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<TestResult> readTestResult(final Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return Optional.ofNullable(mapper.readValue(is, TestResult.class));
        } catch (IOException e) {
            errors.add(new ReadError(e, "Could not read result container file {}", file));
            return Optional.empty();
        }
    }

    public List<ReadError> getErrors() {
        return errors;
    }

    private Stream<Path> listFiles(final Path directory) {
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, TEST_RESULT_FILE_GLOB)) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList())
                    .stream();
        } catch (IOException e) {
            errors.add(new ReadError(e, "Could not list files in directory {}", directory));
            return Stream.empty();
        }
    }

    private void convert(final Supplier<String> uidGenerator,
                         final Path resultsDirectory,
                         final ResultsVisitor visitor,
                         final List<TestResult> fixtures,
                         final TestResult result) {
        final io.qameta.allure.entity.TestResult dest = new io.qameta.allure.entity.TestResult();
        dest.setUid(uidGenerator.get());
        dest.setHistoryId(result.getHistoryId());
        dest.setFullName(result.getFullName());
        dest.setName(firstNonNull(result.getName(), result.getFullName(), "Unknown test"));
        dest.setTime(Time.create(result.getStart(), result.getStop()));
        dest.setDescription(result.getDescription());
        dest.setDescriptionHtml(result.getDescriptionHtml());
        dest.setStatus(convert(result.getStatus()));
        dest.setStatusMessage(result.getStatusMessage());
        dest.setStatusTrace(result.getStatusTrace());
        dest.setLinks(convert(result.getLinks(), this::convert));
        dest.setLabels(convert(result.getLabels(), this::convert));
        dest.setParameters(getParameters(result));

        dest.addLabelIfNotExists(RESULT_FORMAT, ALLURE3_RESULTS_FORMAT);

        if (hasTestStage(result)) {
            dest.setTestStage(getTestStage(resultsDirectory, visitor, result));
        }

        final List<TestResult> testFixtures = findAllFixtures(fixtures, result.getUuid(), new HashSet<>());
        dest.getBeforeStages().addAll(getBefore(resultsDirectory, visitor, testFixtures));
        dest.getAfterStages().addAll(getAfter(resultsDirectory, visitor, testFixtures));
        visitor.visitTestResult(dest);
    }

    private <T, R> List<R> convert(final Collection<T> source, final Function<T, R> converter) {
        return Objects.isNull(source) ? Collections.emptyList() : source.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    private StageResult convert(final Path source,
                                final ResultsVisitor visitor,
                                final TestResult result) {
        return new StageResult()
                .setName(result.getName())
                .setTime(convert(result.getStart(), result.getStop()))
                .setStatus(convert(result.getStatus()))
                .setSteps(convert(result.getSteps(), step -> convert(source, visitor, step)))
                .setAttachments(convert(result.getAttachments(), attach -> convert(source, visitor, attach)))
                .setParameters(convert(result.getParameters(), this::convert))
                .setStatusMessage(result.getStatusMessage())
                .setStatusTrace(result.getStatusTrace());
    }

    private Link convert(final io.qameta.allure.model3.Link link) {
        return new Link()
                .setName(link.getName())
                .setType(link.getType())
                .setUrl(link.getUrl());
    }

    private Label convert(final io.qameta.allure.model3.Label label) {
        return new Label()
                .setName(label.getName())
                .setValue(label.getValue());
    }

    private Parameter convert(final io.qameta.allure.model3.Parameter parameter) {
        return new Parameter()
                .setName(parameter.getName())
                .setValue(parameter.getValue());
    }

    private Attachment convert(final Path source,
                               final ResultsVisitor visitor,
                               final io.qameta.allure.model3.Attachment attachment) {
        final Path attachmentFile = source.resolve(attachment.getSource());
        if (Files.isRegularFile(attachmentFile)) {
            final Attachment found = visitor.visitAttachmentFile(attachmentFile);
            if (nonNull(attachment.getContentType())) {
                found.setType(attachment.getContentType());
            }
            if (nonNull(attachment.getName())) {
                found.setName(attachment.getName());
            }
            return found;
        } else {
            visitor.error("Could not find attachment " + attachment.getSource() + " in directory " + source);
            return new Attachment()
                    .setType(attachment.getContentType())
                    .setName(attachment.getName())
                    .setSize(0L);
        }
    }

    private Step convert(final Path source,
                         final ResultsVisitor visitor,
                         final StepResult step) {
        final Step result = new Step();
        result.setName(step.getName());
        result.setStatus(convert(step.getStatus()));
        result.setTime(convert(step.getStart(), step.getStop()));
        result.setParameters(convert(step.getParameters(), this::convert));
        result.setAttachments(convert(step.getAttachments(), attachment -> convert(source, visitor, attachment)));
        result.setSteps(convert(step.getSteps(), s -> convert(source, visitor, s)));
        result.setStatusMessage(step.getStatusMessage());
        result.setStatusTrace(step.getStatusTrace());
        return result;
    }

    private Status convert(final io.qameta.allure.model3.Status status) {
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
        StageResult testStage = new StageResult();
        testStage.setSteps(convert(result.getSteps(), step -> convert(source, visitor, step)));
        testStage.setAttachments(convert(result.getAttachments(), attachment -> convert(source, visitor, attachment)));
        testStage.setStatus(convert(result.getStatus()));

        testStage.setStatusMessage(result.getStatusMessage());
        testStage.setStatusTrace(result.getStatusTrace());
        return testStage;
    }

    private boolean hasTestStage(final TestResult result) {
        return !result.getSteps().isEmpty() || !result.getAttachments().isEmpty();
    }

    private List<StageResult> getBefore(final Path source,
                                        final ResultsVisitor visitor,
                                        final List<TestResult> testFixtures) {
        return convert(
                testFixtures.stream().filter(
                    fixture -> TestResultType.SET_UP.equals(fixture.getType())
                ).collect(Collectors.toList()),
            fixture -> convert(source, visitor, fixture)
        ).stream().sorted(BY_START).collect(Collectors.toList());
    }

    private List<StageResult> getAfter(final Path source,
                                        final ResultsVisitor visitor,
                                        final List<TestResult> testFixtures) {
        return convert(
                testFixtures.stream().filter(
                    fixture -> TestResultType.TEAR_DOWN.equals(fixture.getType())
                ).collect(Collectors.toList()),
            fixture -> convert(source, visitor, fixture)
        ).stream().sorted(BY_START).collect(Collectors.toList());
    }

    private List<TestResult> findAllFixtures(final List<TestResult> fixtures,
                                                     final String id,
                                                     final Set<String> seen) {
        final List<TestResult> result = new ArrayList<>();
        final List<TestResult> resultFixtures = findFixteres(fixtures, id, seen);
        result.addAll(resultFixtures);
        for (TestResult fixture : resultFixtures) {
            result.addAll(findAllFixtures(fixtures, fixture.getUuid(), seen));
        }
        return result;
    }

    private List<TestResult> findFixteres(final List<TestResult> fixtures,
                                                  final String id,
                                                  final Set<String> seen) {
        return fixtures.stream()
                .filter(fixture -> fixture.getChildren().contains(id))
                .filter(fixture -> !seen.contains(fixture.getUuid()))
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
