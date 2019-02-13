/*
 *  Copyright 2019 Qameta Software OÜ
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
package io.qameta.allure.cucumberjson;

import com.google.inject.Inject;
import io.qameta.allure.AttachmentsStorage;
import io.qameta.allure.ResultsProcessor;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.StatusDetails;
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

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.ReportApiUtils.generateUid;
import static io.qameta.allure.ReportApiUtils.listFiles;
import static net.masterthought.cucumber.json.support.Status.FAILED;
import static net.masterthought.cucumber.json.support.Status.PENDING;
import static net.masterthought.cucumber.json.support.Status.SKIPPED;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class CucumberJsonResultsReader implements ResultsProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberJsonResultsReader.class);

    private final AttachmentsStorage storage;

    @Inject
    public CucumberJsonResultsReader(final AttachmentsStorage storage) {
        this.storage = storage;
    }

    @Override
    public List<TestCaseResult> readResults(final Path source) {
        List<String> cucumberJsonFiles = listFiles(source, "cucumber*.json")
                .map(Path::toString)
                .collect(Collectors.toList());
        if (cucumberJsonFiles.isEmpty()) {
            return Collections.emptyList();
        }
        Configuration configuration = new Configuration(source.toFile(), "Unknown project");
        File embeddings = configuration.getEmbeddingDirectory();
        if (!embeddings.exists() && !embeddings.mkdirs()) {
            LOGGER.warn("Could not create embeddings directory");
        }
        ReportParser parser = new ReportParser(configuration);
        List<Feature> features = parse(parser, cucumberJsonFiles);
        listFiles(embeddings.toPath(), "embedding*")
                .forEach(storage::addAttachment);
        return features.stream()
                .flatMap(feature -> Stream.of(feature.getElements()).map(this::convert))
                .collect(Collectors.toList());
    }

    private TestCaseResult convert(final Element element) {
        TestCaseResult result = new TestCaseResult();
        String testName = Optional.ofNullable(element.getName()).orElse("Unnamed scenario");
        String featureName = Optional.ofNullable(element.getFeature().getName()).orElse("Unnamed feature");
        result.setTestCaseId(String.format("%s#%s", featureName, testName));
        result.setUid(generateUid());
        result.setName(firstNonNull(element.getName(), element.getKeyword(), element.getDescription(), "Unknown"));
        result.setTime(getTime(element));
        result.setStatus(getStatus(element));
        result.setStatusDetails(getStatusDetails(element));
        result.addLabelIfNotExists(LabelName.FEATURE, element.getFeature().getName());
        result.setDescription(element.getDescription());

        if (Objects.nonNull(element.getSteps()) && element.getSteps().length > 0) {
            result.setTestStage(getTestStage(element));
        }

        if (Objects.nonNull(element.getBefore())) {
            result.setBeforeStages(convertHooks(element.getBefore()));
        }

        if (Objects.nonNull(element.getAfter())) {
            result.setAfterStages(convertHooks(element.getAfter()));
        }
        return result;
    }

    private long getTime(final Element source) {
        return Stream.of(source.getSteps())
                .map(Step::getDuration)
                .reduce((t1, t2) -> t1 + t2).orElse(0L);
    }

    private Status getStatus(final Element source) {
        if (source.getStatus().isPassed()) {
            return Status.PASSED;
        }
        if (Objects.nonNull(source.getSteps())) {
            return Stream.of(source.getSteps())
                    .map(this::getStepStatus)
                    .min(Enum::compareTo)
                    .orElse(Status.UNKNOWN);
        }
        return getStatus(source.getStatus());
    }

    @SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
    private Status getStatus(final net.masterthought.cucumber.json.support.Status status) {
        if (status.isPassed()) {
            return Status.PASSED;
        }
        if (status == FAILED) {
            return Status.FAILED;
        }
        if (status == PENDING) {
            return Status.SKIPPED;
        }
        if (status == SKIPPED) {
            return Status.SKIPPED;
        }
        return Status.UNKNOWN;
    }

    private StatusDetails getStatusDetails(final Element source) {
        return Stream.of(source.getSteps())
                .map(Step::getResult)
                .map(this::getStatusDetails)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private StatusDetails getStatusDetails(final Result result) {
        return Optional.ofNullable(result.getErrorMessage())
                .map(msg -> new StatusDetails().withMessage(msg))
                .orElse(null);
    }

    private StageResult getTestStage(final Element source) {
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
                                .withStatusDetails(getStatusDetails(step.getResult()))
                        )
                        .collect(Collectors.toList()))
                .withStatusDetails(getStatusDetails(source));
    }

    private List<StageResult> convertHooks(final Hook... hooks) {
        return Stream.of(hooks)
                .map(hook -> new StageResult()
                        .withAttachments(
                                Stream.of(hook.getEmbeddings())
                                        .map(this::convertAttachment)
                                        .collect(Collectors.toList()))
                        .withStatusDetails(getStatusDetails(hook.getResult()))
                        .withTime(new Time().withDuration(hook.getResult().getDuration()))
                ).collect(Collectors.toList());
    }

    private Attachment convertAttachment(final Embedding embedding) {
        Attachment found = storage.findAttachmentByFileName(embedding.getFileName())
                .orElseGet(() -> new Attachment().withName("unknown").withSize(0L).withType("*/*"));
        if (Objects.nonNull(embedding.getMimeType())) {
            found.setType(embedding.getMimeType());
        }
        return found;
    }

    private Status getStepStatus(final Step step) {
        return getStatus(step.getResult().getStatus());
    }

    @SafeVarargs
    private final <T> T firstNonNull(final T... items) {
        return Stream.of(items)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("At least one item should be non-null"));
    }

    private List<Feature> parse(final ReportParser parser, final List<String> cucumberJsonFiles) {
        try {
            return cucumberJsonFiles.isEmpty()
                    ? Collections.emptyList()
                    : parser.parseJsonFiles(cucumberJsonFiles);
        } catch (Exception e) {
            LOGGER.error("Could not parse results {}", e);
            return Collections.emptyList();
        }
    }
}
