package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import javafx.beans.binding.SetBinding;

import java.nio.file.Path;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 30.01.16
 */
public class BootstrapModule extends AbstractModule {

    private final Path[] inputDirectories;

    public BootstrapModule(Path... inputDirectories) {
        this.inputDirectories = inputDirectories;
    }

    @Override
    protected void configure() {
        bind(Path[].class).annotatedWith(ResultsDirectories.class).toInstance(inputDirectories);

        MapBinder.newMapBinder(binder(), String.class, Aggregator.class);
        MapBinder.newMapBinder(binder(), String.class, Processor.class);
        MapBinder.newMapBinder(binder(), String.class, String.class, ReportFilesNamesMap.class);
        MapBinder.newMapBinder(binder(), String.class, String.class, WidgetsNamesMap.class);

        MapBinder.newMapBinder(binder(), String.class, Finalizer.class, WidgetDataFinalizer.class);

        Multibinder.newSetBinder(binder(), String.class, PluginNames.class);
        Multibinder.newSetBinder(binder(), ResultsProcessor.class);
    }
}
