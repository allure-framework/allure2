package org.allurefw.report.tms;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.TestCasePreparer;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 18.02.16
 */
public class TmsModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCasePreparer.class)
                .addBinding().to(TmsPlugin.class);
    }
}
