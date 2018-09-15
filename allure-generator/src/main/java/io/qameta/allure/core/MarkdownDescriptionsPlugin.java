package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.entity.StageResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Plugin that converts descriptions from markdown to html.
 *
 * @since 2.0
 */
public class MarkdownDescriptionsPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        configuration.getContext(MarkdownContext.class)
                .ifPresent(markdownContext -> processDescriptions(launchesResults, markdownContext));
    }

    private void processDescriptions(final List<LaunchResults> launches, final MarkdownContext context) {
        launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .peek(result -> {
                    if (result.getDescription() == null) {
                        result.setDescription("");
                    }
                    if (result.getDescriptionHtml() == null) {
                        result.setDescriptionHtml("");
                    }
                })
                .peek(result -> {
                    String fixturesDescriptionHtml = result.getBeforeStages().stream()
                            .map(StageResult::getDescriptionHtml)
                            .filter(stageResult -> !isEmpty(stageResult))
                            .collect(Collectors.joining("</br>"));
                    if (!isEmpty(fixturesDescriptionHtml)) {
                        if (isEmpty(result.getDescriptionHtml())) {
                            result.setDescriptionHtml(fixturesDescriptionHtml);
                        } else {
                            result.setDescriptionHtml(fixturesDescriptionHtml + "</br>" + result.getDescriptionHtml());
                        }
                    }
                })
                .filter(result -> isEmpty(result.getDescriptionHtml()) && !isEmpty(result.getDescription()))
                .forEach(result -> {
                    final String html = context.getValue().apply(result.getDescription());
                    result.setDescriptionHtml(html);
                });
    }

    private static boolean isEmpty(final String string) {
        return Objects.isNull(string) || string.isEmpty();
    }

}
