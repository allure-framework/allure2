package org.allurefw.report.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.AllureConstants;
import io.qameta.allure.AllureUtils;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.TestAfterResult;
import io.qameta.allure.model.TestBeforeResult;
import io.qameta.allure.model.TestGroupResult;
import io.qameta.allure.model.TestStepResult;
import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.ResultsReader;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.Label;
import org.allurefw.report.entity.Link;
import org.allurefw.report.entity.Parameter;
import org.allurefw.report.entity.StageResult;
import org.allurefw.report.entity.Status;
import org.allurefw.report.entity.Step;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.Time;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.allurefw.report.ReportApiUtils.generateUid;
import static org.allurefw.report.ReportApiUtils.listFiles;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure2ResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Allure2ResultsReader.class);

    private final AttachmentsStorage storage;

    private final ObjectMapper mapper = AllureUtils.createMapper();

    @Inject
    public Allure2ResultsReader(AttachmentsStorage storage) {
        this.storage = storage;
    }

    @Override
    public List<TestCaseResult> readResults(Path source) {
        Map<String, TestGroupResult> groups = listFiles(source, AllureConstants.TEST_GROUP_JSON_FILE_GLOB)
                .flatMap(this::readTestGroupResult)
                .collect(Collectors.toMap(TestGroupResult::getId, Function.identity()));


        return listFiles(source, AllureConstants.TEST_CASE_JSON_FILE_GLOB)
                .flatMap(this::readTestCaseResult)
                .map(result -> {
                    TestCaseResult dest = new TestCaseResult();
                    dest.setUid(generateUid());

                    dest.setId(result.getId());
                    dest.setName(result.getName());
                    dest.setTime(result.getStart(), result.getStop());
                    dest.setDescription(result.getDescription());
                    dest.setDescriptionHtml(result.getDescriptionHtml());
                    dest.setStatus(convert(result.getStatus()));
                    dest.setFailure(convert(result.getStatusDetails()));

                    dest.setLinks(convert(result.getLinks(), this::convert));
                    dest.setLabels(convert(result.getLabels(), this::convert));
                    dest.setParameters(convert(result.getParameters(), this::convert));

                    if (!result.getSteps().isEmpty() || !result.getAttachments().isEmpty()) {
                        StageResult testStage = new StageResult();
                        testStage.setSteps(convert(result.getSteps(), this::convert));
                        testStage.setAttachments(convert(result.getAttachments(), this::convert));
                        testStage.setFailure(convert(result.getStatusDetails()));
                        dest.setTestStage(testStage);
                    }

                    dest.getBeforeStages().addAll(getParentBefores(result.getTestGroupId(), groups));
                    dest.getBeforeStages().addAll(convert(result.getBefores(), this::convert));
                    dest.getAfterStages().addAll(convert(result.getAfters(), this::convert));
                    dest.getAfterStages().addAll(getParentAfters(result.getTestGroupId(), groups));
                    return dest;
                }).collect(Collectors.toList());
    }

    private List<StageResult> getParentBefores(String parentId, Map<String, TestGroupResult> groups) {
        return getParentBefores(parentId, groups, new HashSet<>());
    }

    private List<StageResult> getParentBefores(String parentId, Map<String, TestGroupResult> groups, Set<String> seen) {
        if (Objects.nonNull(parentId) && groups.containsKey(parentId) && seen.add(parentId)) {
            TestGroupResult result = groups.get(parentId);
            List<StageResult> results = getParentBefores(result.getParentId(), groups, seen);
            results.addAll(convert(result.getBefores(), this::convert));
            return results;
        } else {
            return new ArrayList<>();
        }
    }

    private List<StageResult> getParentAfters(String parentId, Map<String, TestGroupResult> groups) {
        return getParentAfters(parentId, groups, new HashSet<>());
    }

    private List<StageResult> getParentAfters(String parentId, Map<String, TestGroupResult> groups, Set<String> seen) {
        if (Objects.nonNull(parentId) && groups.containsKey(parentId) && seen.add(parentId)) {
            TestGroupResult result = groups.get(parentId);
            List<StageResult> results = convert(result.getAfters(), this::convert);
            results.addAll(getParentAfters(result.getParentId(), groups, seen));
            return results;
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
        return storage.findAttachmentByFileName(attachment.getSource())
                .map(attach -> Objects.isNull(attachment.getType()) ? attach : attach.withType(attachment.getType()))
                .map(attach -> Objects.isNull(attachment.getName()) ? attach : attach.withName(attachment.getName()))
                .orElseGet(Attachment::new);
    }

    private Step convert(TestStepResult step) {
        return new Step()
                .withName(step.getName())
                .withStatus(convert(step.getStatus()))
                .withFailure(convert(step.getStatusDetails()))
                .withTime(convert(step.getStart(), step.getStop()))
                .withParameters(convert(step.getParameters(), this::convert))
                .withAttachments(convert(step.getAttachments(), this::convert))
                .withSteps(convert(step.getSteps(), this::convert));
    }

    private Status convert(io.qameta.allure.model.Status status) {
        return Status.fromValue(status.value());
    }

    private Failure convert(StatusDetails details) {
        return Objects.isNull(details) ? null : new Failure()
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
