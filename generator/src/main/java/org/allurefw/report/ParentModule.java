package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import org.allurefw.report.allure1.Allure1ResultsReader;
import org.allurefw.report.allure2.Allure2ResultsReader;
import org.allurefw.report.defects.DefectsPlugin;
import org.allurefw.report.graph.GraphPlugin;
import org.allurefw.report.jackson.JacksonMapperModule;
import org.allurefw.report.timeline.TimelinePlugin;
import org.allurefw.report.total.TotalPlugin;
import org.allurefw.report.writer.WriterModule;
import org.allurefw.report.xunit.XunitPlugin;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class ParentModule extends AbstractModule {

    private final List<Plugin> plugins;

    private final List<Module> children;

    public ParentModule(List<Plugin> plugins, List<Module> children) {
        this.plugins = plugins;
        this.children = children;
    }

    @Override
    protected void configure() {
//        Core
        install(new JacksonMapperModule());
        install(new WriterModule());

        MapBinder.newMapBinder(binder(), String.class, Aggregator.class);
        MapBinder.newMapBinder(binder(), String.class, Processor.class);
        MapBinder.newMapBinder(binder(), String.class, String.class, DataNamesMap.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, String.class, WidgetsNamesMap.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, Finalizer.class);

//        Readers
        Multibinder.newSetBinder(binder(), TestCaseResultsReader.class)
                .addBinding().to(Allure1ResultsReader.class);
        Multibinder.newSetBinder(binder(), TestCaseResultsReader.class)
                .addBinding().to(Allure2ResultsReader.class);

//        Attachments
        OptionalBinder.newOptionalBinder(binder(), AttachmentsStorage.class)
                .setDefault().to(DefaultAttachmentsStorage.class).in(Scopes.SINGLETON);

//        Defaults
        install(new TotalPlugin());
        install(new GraphPlugin());
        install(new TimelinePlugin());
        install(new DefectsPlugin());
        install(new XunitPlugin());

//        Plugins
        Multibinder.newSetBinder(binder(), Plugin.class);
        plugins.forEach(this::bindPlugin);

//        Children
        children.forEach(this::install);
    }

    private void bindPlugin(Plugin plugin) {
        Multibinder.newSetBinder(binder(), Plugin.class).addBinding().toInstance(plugin);
    }
}
