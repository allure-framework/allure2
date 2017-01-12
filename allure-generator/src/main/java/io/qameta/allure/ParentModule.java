package io.qameta.allure;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import io.qameta.allure.allure1.Allure1ResultsReader;
import io.qameta.allure.allure2.Allure2ResultsReader;
import io.qameta.allure.core.DefaultAttachmentsStorage;
import io.qameta.allure.core.DefaultTestRunReader;
import io.qameta.allure.defects.DefectsPlugin;
import io.qameta.allure.executor.ExecutorPlugin;
import io.qameta.allure.graph.GraphPlugin;
import io.qameta.allure.history.HistoryPlugin;
import io.qameta.allure.jackson.JacksonMapperModule;
import io.qameta.allure.markdown.MarkdownPlugin;
import io.qameta.allure.severity.SeverityPlugin;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.testrun.TestRunPlugin;
import io.qameta.allure.timeline.TimelinePlugin;
import io.qameta.allure.xunit.XunitPlugin;

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

//        Aggregators
        MapBinder.newMapBinder(binder(), String.class, TestRunAggregator.class);
        MapBinder.newMapBinder(binder(), String.class, TestCaseAggregator.class);
        MapBinder.newMapBinder(binder(), String.class, ResultAggregator.class);


        MapBinder.newMapBinder(binder(), String.class, Processor.class);
        MapBinder.newMapBinder(binder(), String.class, String.class, Names.named("report-widgets"))
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, String.class, Names.named("report-data-folder"))
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
        install(new SummaryPlugin());
        install(new GraphPlugin());
        install(new TimelinePlugin());
        install(new DefectsPlugin());
        install(new XunitPlugin());
        install(new HistoryPlugin());
        install(new ExecutorPlugin());
        install(new TestRunPlugin());
        install(new SeverityPlugin());
        install(new MarkdownPlugin());

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
