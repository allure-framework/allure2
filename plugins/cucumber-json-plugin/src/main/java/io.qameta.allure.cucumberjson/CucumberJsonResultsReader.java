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
        String cucumberProjectName = "unused parameter";
        Configuration configuration = new Configuration(source.toFile(), cucumberProjectName);
        if (!configuration.getEmbeddingDirectory().mkdirs()) {
            LOGGER.warn("Cannot prepare directory for parser to store embedded files at {}",
                    configuration.getEmbeddingDirectory());
        }

        ReportParser parser = new ReportParser(configuration);
        Stream<Feature> features = parse(parser, source);
        listFiles(configuration.getEmbeddingDirectory().toPath(), "embedding*")
                .forEach(storage::addAttachment);
        List<TestCaseResult> testCaseResults = features
                .flatMap(feature -> Stream.of(feature.getElements())
                        .map(this::convert))
                .collect(Collectors.toList());
        LOGGER.info("Found {} test cases", testCaseResults.size());
        return testCaseResults;
    }

    private TestCaseResult convert(Element element) {
        TestCaseResult testCaseResult = new TestCaseResult();
        String testName = Optional.ofNullable(element.getName()).orElse("Unnamed scenario");
        String featureName = Optional.ofNullable(element.getFeature().getName()).orElse("Unnamed feature");
        LOGGER.debug("Starting to process test case result {} for feature {}", testName, featureName);
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
        LOGGER.debug("Processed test case result {} for feature {}", testName, featureName);
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
        if (Stream.of(source.getSteps()).anyMatch(this::isStepFailed)) {
            return Status.FAILED;
        }
        //if none of the steps were executed, mark element as canceled
        if (Stream.of(source.getSteps()).allMatch(step -> step.getResult().getStatus() == SKIPPED)) {
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
                .map(msg -> new Failure().withMessage(msg)).orElse(null);
    }

    private Status getStepStatus(Step step) {
        if (step.getResult().getStatus().isPassed()) {
            return Status.PASSED;
        }
        if (isStepFailed(step)) {
            return Status.FAILED;
        }
        if (step.getResult().getStatus() == SKIPPED) {
            return Status.CANCELED;
        }
        return Status.BROKEN;
    }

    private boolean isStepFailed(Step step) {
        return step.getResult().getStatus() == FAILED && Optional.ofNullable(step.getResult().getErrorMessage())
                .map(msg -> msg.startsWith("java.lang.AssertionError"))
                .orElse(false);
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
