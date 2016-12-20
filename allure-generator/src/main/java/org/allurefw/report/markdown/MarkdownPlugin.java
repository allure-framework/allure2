package org.allurefw.report.markdown;

import com.google.inject.Scopes;
import org.allurefw.report.AbstractPlugin;

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
