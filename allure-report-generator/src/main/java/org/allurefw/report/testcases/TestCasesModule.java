package org.allurefw.report.testcases;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class TestCasesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TestCasesStorage.class).in(Scopes.SINGLETON);
    }

}
