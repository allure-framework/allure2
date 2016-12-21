package io.qameta.allure.markdown;

import com.google.inject.Scopes;
import io.qameta.allure.AbstractPlugin;

/**
 * @author charlie (Dmitry Baev).
 */
public class MarkdownPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        bind(MarkdownSupport.class).in(Scopes.SINGLETON);
        processor(MarkdownProcessor.class);
    }
}
