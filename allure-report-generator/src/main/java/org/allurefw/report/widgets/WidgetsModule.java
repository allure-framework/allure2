package org.allurefw.report.widgets;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.ReportDataProvider;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class WidgetsModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ReportDataProvider.class)
                .addBinding().to(WidgetsDataProvider.class);
    }

}
