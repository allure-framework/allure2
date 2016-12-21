package io.qameta.allure.writer;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 17.02.16
 */
public class WriterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Writer.class).to(DefaultWriter.class).in(Scopes.SINGLETON);
    }
}
