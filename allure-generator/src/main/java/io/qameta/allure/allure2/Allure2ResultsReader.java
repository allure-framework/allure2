package io.qameta.allure.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.qameta.allure.AllureConstants;
import io.qameta.allure.AllureUtils;
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
import io.qameta.allure.model.TestAfterResult;
import io.qameta.allure.model.TestBeforeResult;
import io.qameta.allure.model.TestGroupResult;
import io.qameta.allure.model.TestStepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.ReportApiUtils.listFiles;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure2ResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2ResultsReader.class);

    private final AttachmentsStorage storage;

    private final ObjectMapper mapper;

    @Inject
    public Allure2ResultsReader(AttachmentsStorage storage) {
        this.storage = storage;
        this.mapper = AllureUtils.createMapper().registerModule(new SimpleModule()
                .addDeserializer(io.qameta.allure.model.Status.class, new StatusDeserializer())
        );
    }

    @Override
    public List<TestCaseResult> readResults(Path source) {
        listFiles(source, "*-attachment*")
                .forEach(storage::addAttachment);

        Map<String, TestGroupResult> groups = listFiles(source, AllureConstants.TEST_GROUP_JSON_FILE_GLOB)
                .flatMap(this::readTestGroupResult)
                .collect(Collectors.toMap(TestGroupResult::getId, Function.identity()));

        return listFiles(source, AllureConstants.TEST_CASE_JSON_FILE_GLOB)
                .flatMap(this::readTestCaseResult)
                .map(result -> {
                    TestCaseResult dest = new TestCaseResult();
                    dest.setUid(generateUid());

                    dest.setTestCaseId(result.getId());
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

                    if (!result.getSteps().isEmpty() || !result.getAttachments().isEmpty()) {
                        StageResult testStage = new StageResult();
                        testStage.setSteps(convert(result.getSteps(), this::convert));
                        testStage.setAttachments(convert(result.getAttachments(), this::convert));
                        testStage.setStatus(convert(result.getStatus()));
                        testStage.setStatusDetails(convert(result.getStatusDetails()));
                        dest.setTestStage(testStage);
                    }

                    dest.getBeforeStages().addAll(getParentBefores(result.getParentIds(), groups));
                    dest.getAfterStages().addAll(getParentAfters(result.getParentIds(), groups));
                    return dest;
                }).collect(Collectors.toList());
    }

    private List<StageResult> getParentBefores(List<String> parents, Map<String, TestGroupResult> groups) {
        return getFromParents(parents, (result, stageResults) -> {
            stageResults.addAll(convert(result.getBefores(), this::convert));
            return stageResults;
        }, groups, new HashSet<>());
    }

    private List<StageResult> getParentAfters(List<String> parents, Map<String, TestGroupResult> groups) {
        return getFromParents(parents, (result, stageResults) -> {
            List<StageResult> current = convert(result.getAfters(), this::convert);
            current.addAll(stageResults);
            return current;
        }, groups, new HashSet<>());
    }

    private <T> List<T> getFromParents(List<String> parents, BiFunction<TestGroupResult, List<T>, List<T>> getter,
                                       Map<String, TestGroupResult> groups, Set<String> seen) {
        return parents.stream()
                .flatMap(parentId -> getFromParents(parentId, getter, groups, seen).stream())
                .collect(Collectors.toList());
    }

    private <T> List<T> getFromParents(String parentId, BiFunction<TestGroupResult, List<T>, List<T>> getter,
                                       Map<String, TestGroupResult> groups, Set<String> seen) {
        if (Objects.nonNull(parentId) && groups.containsKey(parentId) && seen.add(parentId)) {
            TestGroupResult result = groups.get(parentId);
            List<T> fromParents = getFromParents(result.getParentIds(), getter, groups, seen);
            return getter.apply(result, fromParents);
        } else {
            return new ArrayList<>();
        }
    }

    private <T, R> List<R> convert(List<T> source, Function<T, R> converter) {
        return Objects.isNull(source) ? Collections.emptyList() : source.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    private StageResult convert(TestBeforeResult result) {
        return new StageResult()
                .withName(result.getName())
                .withTime(convert(result.getStart(), result.getStop()))
                .withSteps(convert(result.getSteps(), this::convert))
                .withAttachments(convert(result.getAttachments(), this::convert));

    }

    private StageResult convert(TestAfterResult result) {
        return new StageResult()
                .withName(result.getName())
                .withTime(convert(result.getStart(), result.getStop()))
                .withSteps(convert(result.getSteps(), this::convert))
                .withAttachments(convert(result.getAttachments(), this::convert));

    }

    private Link convert(io.qameta.allure.model.Link link) {
        return new Link()
                .withName(link.getName())
                .withType(link.getType())
                .withUrl(link.getUrl());
    }

    private Label convert(io.qameta.allure.model.Label label) {
        return new Label()
                .withName(label.getName())
                .withValue(label.getValue());
    }

    private Parameter convert(io.qameta.allure.model.Parameter parameter) {
        return new Parameter()
                .withName(parameter.getName())
                .withValue(parameter.getValue());
    }

    private Attachment convert(io.qameta.allure.model.Attachment attachment) {
        Attachment found = storage.findAttachmentByFileName(attachment.getSource())
                .map(attach -> new Attachment()
                        .withUid(attach.getUid())
                        .withName(attach.getName())
                        .withType(attach.getType())
                        .withSource(attach.getSource())
                        .withSize(attach.getSize()))
                .orElseGet(() -> new Attachment().withName("unknown").withSize(0L).withType("*/*"));

        if (Objects.nonNull(attachment.getType())) {
            found.setType(attachment.getType());
        }
        if (Objects.nonNull(attachment.getName())) {
            found.setName(attachment.getName());
        }
        return found;
    }

    private Step convert(TestStepResult step) {
        return new Step()
                .withName(step.getName())
                .withStatus(convert(step.getStatus()))
                .withStatusDetails(convert(step.getStatusDetails()))
                .withTime(convert(step.getStart(), step.getStop()))
                .withParameters(convert(step.getParameters(), this::convert))
                .withAttachments(convert(step.getAttachments(), this::convert))
                .withSteps(convert(step.getSteps(), this::convert));
    }

    private Status convert(io.qameta.allure.model.Status status) {
        if (Objects.isNull(status)) {
            return Status.UNKNOWN;
        }
        return Stream.of(Status.values())
                .filter(item -> item.value().equalsIgnoreCase(status.value()))
                .findAny()
                .orElse(Status.UNKNOWN);
    }

    private StatusDetails convert(io.qameta.allure.model.StatusDetails details) {
        return Objects.isNull(details) ? null : new StatusDetails()
                .withMessage(details.getMessage())
                .withTrace(details.getTrace());
    }

    private Time convert(Long start, Long stop) {
        return new Time().withStart(start).withStop(stop).withDuration(stop - start);
    }

    private Stream<io.qameta.allure.model.TestCaseResult> readTestCaseResult(Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Stream.of(mapper.readValue(is, io.qameta.allure.model.TestCaseResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read test case result {}: {}", source, e);
            return Stream.empty();
        }
    }

    private Stream<TestGroupResult> readTestGroupResult(Path source) {
        try (InputStream is = Files.newInputStream(source)) {
            return Stream.of(mapper.readValue(is, TestGroupResult.class));
        } catch (IOException e) {
            LOGGER.debug("Could not read test group result {}: {}", source, e);
            return Stream.empty();
        }
    }
}
