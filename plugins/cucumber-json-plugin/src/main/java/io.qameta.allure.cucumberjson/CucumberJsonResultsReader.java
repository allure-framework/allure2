package io.qameta.allure.cucumberjson;

import com.google.inject.Inject;
import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Failure;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.entity.Time;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportParser;
import net.masterthought.cucumber.json.Element;
import net.masterthought.cucumber.json.Embedding;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.json.Hook;
import net.masterthought.cucumber.json.Result;
import net.masterthought.cucumber.json.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.ReportApiUtils.listFiles;
import static net.masterthought.cucumber.json.support.Status.FAILED;
import static net.masterthought.cucumber.json.support.Status.SKIPPED;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class CucumberJsonResultsReader implements ResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberJsonResultsReader.class);

    private final AttachmentsStorage storage;

    @Inject
    public CucumberJsonResultsReader(AttachmentsStorage storage) {
        this.storage = storage;
    }

    @Override
    public List<TestCaseResult> readResults(Path source) {
        Configuration configuration = new Configuration(source.toFile(), "Unknown project");
        if (Files.exists(configuration.getEmbeddingDirectory().toPath())) {
            listFiles(configuration.getEmbeddingDirectory().toPath(), "embedding*")
                    .forEach(storage::addAttachment);
        }

        ReportParser parser = new ReportParser(configuration);
        return parse(parser, source)
                .flatMap(feature -> Stream.of(feature.getElements()).map(this::convert))
                .collect(Collectors.toList());
    }

    private TestCaseResult convert(Element element) {
        TestCaseResult testCaseResult = new TestCaseResult();
        String testName = Optional.ofNullable(element.getName()).orElse("Unnamed scenario");
        String featureName = Optional.ofNullable(element.getFeature().getName()).orElse("Unnamed feature");
        testCaseResult.setId(String.format("%s#%s", featureName, testName));
        testCaseResult.setUid(generateUid());
        testCaseResult.setName(element.getName());
        testCaseResult.setTime(getTime(element));
        testCaseResult.setStatus(getStatus(element));
        testCaseResult.setFailure(getFailure(element));
        testCaseResult.addLabelIfNotExists(LabelName.FEATURE, element.getFeature().getName());
        testCaseResult.setDescription(element.getDescription());

        if (Objects.nonNull(element.getSteps()) && element.getSteps().length > 0) {
            testCaseResult.setTestStage(getTestStage(element));
        }

        if (Objects.nonNull(element.getBefore())) {
            testCaseResult.setBeforeStages(convertHooks(element.getBefore()));
        }

        if (Objects.nonNull(element.getAfter())) {
            testCaseResult.setAfterStages(convertHooks(element.getAfter()));
        }
        return testCaseResult;
    }

    private long getTime(Element source) {
        return Stream.of(source.getSteps())
                .map(Step::getDuration)
                .reduce((t1, t2) -> t1 + t2).orElse(0L);
    }

    private Status getStatus(Element source) {
        if (source.getStatus().isPassed()) {
            return Status.PASSED;
        }
        if (source.getStatus() == FAILED) {
            return Status.FAILED;
        }
        if (source.getStatus() == SKIPPED) {
            return Status.CANCELED;
        }
        return Status.BROKEN;
    }

    private Failure getFailure(Element source) {
        return Stream.of(source.getSteps())
                .map(Step::getResult)
                .map(this::getFailure)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private StageResult getTestStage(Element source) {
        return new StageResult().withSteps(
                Stream.of(source.getSteps())
                        .map(step -> new io.qameta.allure.entity.Step()
                                .withName(step.getName())
                                .withStatus(getStepStatus(step))
                                .withTime(new Time().withDuration(step.getResult().getDuration()))
                                .withAttachments(
                                        Stream.of(step.getEmbeddings())
                                                .map(this::convertAttachment)
                                                .collect(Collectors.toList())
                                )
                                .withFailure(getFailure(step.getResult()))
                        )
                        .collect(Collectors.toList()))
                .withFailure(getFailure(source));
    }

    private List<StageResult> convertHooks(Hook[] hooks) {
        return Stream.of(hooks)
                .map(hook -> new StageResult()
                        .withAttachments(
                                Stream.of(hook.getEmbeddings())
                                        .map(this::convertAttachment)
                                        .collect(Collectors.toList()))
                        .withFailure(getFailure(hook.getResult()))
                        .withTime(new Time().withDuration(hook.getResult().getDuration()))
                ).collect(Collectors.toList());
    }

    private Attachment convertAttachment(Embedding embedding) {
        Attachment found = storage.findAttachmentByFileName(embedding.getFileName())
                .orElseGet(() -> new Attachment().withName("unknown").withSize(0L).withType("*/*"));
        if (Objects.nonNull(embedding.getMimeType())) {
            found.setType(embedding.getMimeType());
        }
        return found;
    }

    private Failure getFailure(Result result) {
        return Optional.ofNullable(result.getErrorMessage())
                .map(msg -> new Failure().withMessage(msg))
                .orElse(null);
    }

    private Status getStepStatus(Step step) {
        Result result = step.getResult();
        if (result.getStatus().isPassed()) {
            return Status.PASSED;
        }
        if (result.getStatus() == FAILED) {
            return Status.FAILED;
        }
        if (result.getStatus() == SKIPPED) {
            return Status.CANCELED;
        }
        return Status.BROKEN;
    }

    private static Stream<Feature> parse(ReportParser parser, Path source) {
        try {
            return parser.parseJsonFiles(listFiles(source, "cucumber*.json")
                    .map(Path::toString)
                    .collect(Collectors.toList())).stream();
        } catch (Exception e) {
            LOGGER.error("Could not parse result {}: {}", source, e);
            return Stream.empty();
        }
    }
}
