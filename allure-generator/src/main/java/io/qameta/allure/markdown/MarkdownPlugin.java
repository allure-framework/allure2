package io.qameta.allure.markdown;

import io.qameta.allure.Configuration;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.Plugin;
import io.qameta.allure.context.MarkdownContext;

import java.nio.file.Path;
import java.util.List;

import static org.parboiled.common.StringUtils.isEmpty;
import static org.parboiled.common.StringUtils.isNotEmpty;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownPlugin implements Plugin {

    @Override
    public void process(final Configuration configuration,
                        final List<LaunchResults> launches,
                        final Path outputDirectory) {
        configuration.getContext(MarkdownContext.class)
                .ifPresent(markdownContext -> processDescriptions(launches, markdownContext));
    }

    private void processDescriptions(final List<LaunchResults> launches, final MarkdownContext context) {
        launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .filter(result -> isNotEmpty(result.getDescriptionHtml()) || isEmpty(result.getDescription()))
                .forEach(result -> {
                    final String html = context.getValue().markdownToHtml(result.getDescription());
                    result.setDescriptionHtml(html);
                });
    }

}
