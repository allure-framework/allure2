package org.allurefw.report.junit;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.TestCaseProvider;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class JunitModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseProvider.class)
                .addBinding().to(JunitTestCaseProvider.class).in(Scopes.SINGLETON);
    }

}
