package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import org.allurefw.report.allure1.Allure1ResultsReader;
import org.allurefw.report.allure2.Allure2ResultsReader;
import org.allurefw.report.jackson.JacksonMapperModule;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class ParentModule extends AbstractModule {

    private final List<Plugin> plugins;

    public ParentModule(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    protected void configure() {
//        Core
        install(new FactoryModuleBuilder()
                .implement(ResultsSource.class, FileSystemResultsSource.class)
                .build(ResultsSourceFactory.class)
        );
        install(new JacksonMapperModule());

//        Readers
        Multibinder.newSetBinder(binder(), TestCaseResultsReader.class)
                .addBinding().to(Allure1ResultsReader.class);
        Multibinder.newSetBinder(binder(), TestCaseResultsReader.class)
                .addBinding().to(Allure2ResultsReader.class);

//        Attachments
        OptionalBinder.newOptionalBinder(binder(), AttachmentsStorage.class)
                .setDefault().to(DefaultAttachmentsStorage.class).in(Scopes.SINGLETON);

//        Plugins
        Multibinder.newSetBinder(binder(), Plugin.class);
        plugins.forEach(this::bindPlugin);
    }

    private void bindPlugin(Plugin plugin) {
        Multibinder.newSetBinder(binder(), Plugin.class).addBinding().toInstance(plugin);
    }
}
