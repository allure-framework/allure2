package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import org.allurefw.report.allure1.Allure1ResultsReader;
import org.allurefw.report.allure2.Allure2ResultsReader;
import org.allurefw.report.core.DefaultAttachmentsStorage;
import org.allurefw.report.defects.DefectsPlugin;
import org.allurefw.report.executor.ExecutorPlugin;
import org.allurefw.report.graph.GraphPlugin;
import org.allurefw.report.history.HistoryPlugin;
import org.allurefw.report.jackson.JacksonMapperModule;
import org.allurefw.report.core.DefaultTestRunReader;
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

//        Aggregators
        MapBinder.newMapBinder(binder(), String.class, TestRunAggregator.class);
        MapBinder.newMapBinder(binder(), String.class, TestCaseAggregator.class);
        MapBinder.newMapBinder(binder(), String.class, ResultAggregator.class);


        MapBinder.newMapBinder(binder(), String.class, Processor.class);
        MapBinder.newMapBinder(binder(), String.class, String.class, DataFileNames.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, String.class, WidgetNames.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, Finalizer.class);

//        Readers
        Multibinder.newSetBinder(binder(), ResultsReader.class)
                .addBinding().to(Allure1ResultsReader.class);
        Multibinder.newSetBinder(binder(), ResultsReader.class)
                .addBinding().to(Allure2ResultsReader.class);

        OptionalBinder.newOptionalBinder(binder(), TestRunReader.class)
                .setDefault().to(DefaultTestRunReader.class);
        Multibinder.newSetBinder(binder(), TestRunDetailsReader.class);

//        Attachments
        OptionalBinder.newOptionalBinder(binder(), AttachmentsStorage.class)
                .setDefault().to(DefaultAttachmentsStorage.class).in(Scopes.SINGLETON);

//        Defaults
        install(new TotalPlugin());
        install(new GraphPlugin());
        install(new TimelinePlugin());
        install(new DefectsPlugin());
        install(new XunitPlugin());
        install(new HistoryPlugin());
        install(new ExecutorPlugin());

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
