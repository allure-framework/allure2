package org.allurefw.report.defects;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.DefectsData;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class DefectsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DefectsData.class).in(Scopes.SINGLETON);

        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(DefectsPlugin.class);

        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(DefectsDataProvider.class);
    }
}
