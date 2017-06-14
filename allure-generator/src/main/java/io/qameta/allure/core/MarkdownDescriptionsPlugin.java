package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.MarkdownContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

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
