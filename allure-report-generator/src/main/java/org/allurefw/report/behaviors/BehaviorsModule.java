package org.allurefw.report.behaviors;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.BehaviorData;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class BehaviorsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BehaviorData.class).in(Scopes.SINGLETON);

        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(BehaviorsPlugin.class);

        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(BehaviorsDataProvider.class);
    }
}
