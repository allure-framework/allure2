package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import org.allurefw.report.allure1.Allure1ResultsReader;
import org.allurefw.report.jackson.JacksonMapperModule;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public class ParentModule extends AbstractModule {

    private final List<Plugin> plugins;

    public ParentModule() {
        this(Collections.emptyList());
    }

    public ParentModule(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(ResultsSource.class, FileSystemResultsSource.class)
                .build(ResultsSourceFactory.class)
        );
        install(new JacksonMapperModule());
        Multibinder.newSetBinder(binder(), ResultsReader.class)
                .addBinding().to(Allure1ResultsReader.class);

        bind(AttachmentsStorage.class).to(DefaultAttachmentsStorage.class);
        plugins.stream()
                .map(Plugin::getModule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::install);
    }
}
