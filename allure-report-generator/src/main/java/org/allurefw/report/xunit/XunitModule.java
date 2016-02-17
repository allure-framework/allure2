package org.allurefw.report.xunit;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.TestCaseProcessor;
import org.allurefw.report.WidgetDataProvider;
import org.allurefw.report.XunitData;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
public class XunitModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(XunitData.class).in(Scopes.SINGLETON);

        Multibinder.newSetBinder(binder(), TestCaseProcessor.class)
                .addBinding().to(XunitPlugin.class);

        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(XunitReportDataProvider.class);

        Multibinder.newSetBinder(binder(), WidgetDataProvider.class)
                .addBinding().to(XunitWidgetDataProvider.class);
    }
}
