package io.qameta.allure.hotspot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.executor.ExecutorPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidThrowingRawExceptionTypes"})
public class HotspotPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final ObjectMapper mapper = configuration.requireContext(JacksonContext.class).getValue();
        final Map<String, Element> elements = new HashMap<>();
        launchesResults.forEach(launch -> {
            final ExecutorInfo executor = launch.getExtra(
                    ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                    ExecutorInfo::new
            );
            launch.getAllResults().forEach(result -> {
                final List<Attachment> attachments = new ArrayList<>();
                result.getBeforeStages().stream()
                        .filter(Objects::nonNull)
                        .map(this::getAttachments)
                        .forEach(attachments::addAll);
                result.getAfterStages().stream()
                        .filter(Objects::nonNull)
                        .map(this::getAttachments)
                        .forEach(attachments::addAll);
                Optional.ofNullable(result.getTestStage())
                        .map(this::getAttachments)
                        .ifPresent(attachments::addAll);

                final List<Attachment> locators = attachments.stream()
                        .filter(attachment -> attachment.getName().endsWith("locators"))
                        .collect(Collectors.toList());

                for (final Attachment attachment : locators) {
                    try {
                        final Path path = outputDirectory.resolve("data").resolve("attachments")
                                .resolve(attachment.getSource());
                        final List<LocatorAction> actions = mapper
                                .readValue(Files.readAllBytes(path), new TypeReference<List<LocatorAction>>() {
                                });
                        System.out.println(actions.size());
                        actions.forEach(action -> {
                            final Element element = elements.getOrDefault(action.getFullPath(), new Element());
                            element.setFullPath(action.getFullPath());
                            element.addUrls(action.getUrls());

                            final Element.Test test = new Element.Test()
                                    .setName(result.getName())
                                    .setDuration(result.getTime().getDuration())
                                    .setStatus(result.getStatus().value());
                            Optional.ofNullable(executor)
                                    .map(ExecutorInfo::getReportUrl)
                                    .map(url -> this.createReportUrl(url, result.getUid()))
                                    .ifPresent(test::setUrl);
                            element.getTests().add(test);
                            elements.put(element.getFullPath(), element);
                        });
                    } catch (IOException e) {
                        e.printStackTrace(System.out);
//                        throw new RuntimeException(e);
                    }
                }
            });
        });
        final Path hotspot = outputDirectory.resolve("export").resolve("hotspot.json");
        Files.write(hotspot, mapper.writeValueAsBytes(elements.values()));
    }

    private List<Attachment> getAttachments(final StageResult stage) {
        final List<Attachment> attachments = new ArrayList<>();
        Optional.ofNullable(stage.getAttachments()).ifPresent(attachments::addAll);
        stage.getSteps().forEach(step -> attachments.addAll(getAttachments(step)));
        return attachments;
    }

    private List<Attachment> getAttachments(final Step steps) {
        final List<Attachment> attachments = new ArrayList<>();
        Optional.ofNullable(steps.getAttachments()).ifPresent(attachments::addAll);
        steps.getSteps().forEach(step -> attachments.addAll(getAttachments(step)));
        return attachments;
    }

    private String createReportUrl(final String reportUrl, final String uuid) {
        final String pattern = reportUrl.endsWith("index.html") ? "%s#testresult/%s" : "%s/#testresult/%s";
        return String.format(pattern, reportUrl, uuid);
    }
}
