package org.allurefw.report.writer;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.allurefw.report.Writer;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class WriterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Writer.class).to(DefaultWriter.class).in(Scopes.SINGLETON);
    }
}
