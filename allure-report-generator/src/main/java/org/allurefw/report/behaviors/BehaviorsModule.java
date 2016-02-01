package org.allurefw.report.behaviors;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.BehaviorData;
import org.allurefw.report.TestCaseProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public class BehaviorsModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(BehaviorsPlugin.class);
    }

    @Provides
    @Singleton
    protected BehaviorData getData() {
        return new BehaviorData();
    }
}
