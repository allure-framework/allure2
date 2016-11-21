package org.allurefw.report.allure2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.AllureConstants;
import io.qameta.allure.AllureUtils;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.TestGroupResult;
import io.qameta.allure.model.TestStepResult;
import org.allurefw.report.AttachmentsStorage;
import org.allurefw.report.ResultsReader;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.Failure;
import org.allurefw.report.entity.Label;
import org.allurefw.report.entity.Link;
import org.allurefw.report.entity.Parameter;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                    TestGroupResult group = groups.getOrDefault(result.getTestGroupId(), defaultGroup());

                    TestCaseResult dest = new TestCaseResult();
                    dest.setUid(generateUid());

                    dest.setId(result.getId());
                    dest.setName(result.getName());
                    dest.setFullName(String.format("%s#%s", group.getName(), dest.getName()));
                    dest.setTime(result.getStart(), result.getStop());
                    dest.setDescription(result.getDescription());
                    dest.setDescriptionHtml(result.getDescriptionHtml());
                    dest.setStatus(convert(result.getStatus()));
                    dest.setFailure(convert(result.getStatusDetails()));

                    dest.setLinks(convert(result.getLinks(), this::convert));
                    dest.setLabels(convert(result.getLabels(), this::convert));
                    dest.setParameters(convert(result.getParameters(), this::convert));
                    dest.setAttachments(convert(result.getAttachments(), this::convert));
                    dest.setSteps(convert(result.getSteps(), this::convert));

                    dest.addLabelIfNotExists(group.getType(), group.getName());
                    return dest;
                }).collect(Collectors.toList());
    }

    private <T, R> List<R> convert(List<T> source, Function<T, R> converter) {
        return source.stream()
                .map(converter)
                .collect(Collectors.toList());
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

    private TestGroupResult defaultGroup() {
        return new TestGroupResult()
                .withName("Unknown Suite")
                .withType("suite");
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
