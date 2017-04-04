package io.qameta.allure.markdown;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.nio.file.Path;
import java.util.List;

import static org.parboiled.common.StringUtils.isEmpty;
import static org.parboiled.common.StringUtils.isNotEmpty;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownAggregator implements Aggregator {

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
                .filter(result -> isEmpty(result.getDescriptionHtml()) && isNotEmpty(result.getDescription()))
                .forEach(result -> {
                    final String html = context.getValue().markdownToHtml(result.getDescription());
                    result.setDescriptionHtml(html);
                });
    }

}
