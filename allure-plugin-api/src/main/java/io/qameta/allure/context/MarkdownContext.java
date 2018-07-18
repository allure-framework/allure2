package io.qameta.allure.context;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import io.qameta.allure.Context;

import java.util.function.Function;

/**
 * Markdown context. Can be used to process markdown files to html.
 *
 * @since 2.0
 */
public class MarkdownContext implements Context<Function<String, String>> {

    @Override
    public Function<String, String> getValue() {
        final Parser parser = Parser.builder().build();
        final HtmlRenderer renderer = HtmlRenderer.builder().build();
        return s -> renderer.render(parser.parse(s));
    }
}
