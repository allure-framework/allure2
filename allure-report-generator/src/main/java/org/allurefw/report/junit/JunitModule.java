package org.allurefw.report.junit;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.ResultsProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class JunitModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ResultsProcessor.class)
                .addBinding().to(JunitResults.class).in(Scopes.SINGLETON);
    }

}
