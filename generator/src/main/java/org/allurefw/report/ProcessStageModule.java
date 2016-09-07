package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;

import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessStageModule extends AbstractModule {

    private final List<Module> plugins;

    public ProcessStageModule(List<Module> plugins) {
        this.plugins = plugins;
    }

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseResult.class);
        MapBinder.newMapBinder(binder(), String.class, TestGroup.class);
        MapBinder.newMapBinder(binder(), String.class, TestGroup.class, Names.named("suite"));
        MapBinder.newMapBinder(binder(), Path.class, Attachment.class);

        MapBinder.newMapBinder(binder(), String.class, Aggregator.class);
        MapBinder.newMapBinder(binder(), String.class, Processor.class);
        MapBinder.newMapBinder(binder(), String.class, String.class, DataNamesMap.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, String.class, WidgetsNamesMap.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, Finalizer.class);

        plugins.forEach(this::install);
    }
}
