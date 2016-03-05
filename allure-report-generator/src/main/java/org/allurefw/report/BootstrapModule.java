package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

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
        //TODO we kinda need to hide this from plugins I guess
        bind(Path[].class).annotatedWith(ResultsDirectories.class).toInstance(inputDirectories);

        MapBinder.newMapBinder(binder(), String.class, Aggregator.class);

        Multibinder.newSetBinder(binder(), ResultsProcessor.class);

        Multibinder.newSetBinder(binder(), TestCasePreparer.class);
        Multibinder.newSetBinder(binder(), TestCaseProcessor.class);

        Multibinder.newSetBinder(binder(), ReportDataProvider.class);
        Multibinder.newSetBinder(binder(), WidgetDataProvider.class);
    }
}
