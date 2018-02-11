package io.qameta.allure.core;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import io.qameta.allure.Aggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.service.TestResultService;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Plugin that converts descriptions from markdown to html.
 *
 * @since 2.0
 */
public class MarkdownDescriptionsPlugin implements Aggregator {

    @Override
    public void aggregate(final ReportContext context, final TestResultService resultService,
                          final Path outputDirectory) {

        final Parser parser = Parser.builder().build();
        final HtmlRenderer renderer = HtmlRenderer.builder().build();

        resultService.findAll().stream()
                .filter(result -> isEmpty(result.getDescriptionHtml()) && !isEmpty(result.getDescription()))
                .forEach(result -> {
                    final String html = renderer.render(parser.parse(result.getDescription()));
                    result.setDescriptionHtml(html);
                });
    }

    private static boolean isEmpty(final String string) {
        return Objects.isNull(string) || string.isEmpty();
    }

}
