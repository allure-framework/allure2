package org.allurefw.report.allure1;

import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginScope;
import org.allurefw.report.ResultsProcessor;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
@Plugin(name = "allure1", scope = PluginScope.READ)
public class Allure1Plugin extends AbstractPlugin {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ResultsProcessor.class)
                .addBinding().to(Allure1Results.class).in(Scopes.SINGLETON);
    }
}
