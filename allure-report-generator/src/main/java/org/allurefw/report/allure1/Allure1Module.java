package org.allurefw.report.allure1;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.Results;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class Allure1Module extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), Results.class)
                .addBinding().to(Allure1Results.class).in(Scopes.SINGLETON);
    }
}
