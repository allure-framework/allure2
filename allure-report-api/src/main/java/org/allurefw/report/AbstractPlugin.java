package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public abstract class AbstractPlugin extends AbstractModule {

    private final Plugin pluginAnnotation = getClass().getAnnotation(Plugin.class);

    public <T> AggregatorBuilder<T> aggregator(Class<? extends Aggregator<T>> aggregatorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, Aggregator.class)
                .addBinding(uid).to(aggregatorClass);

        return new AggregatorBuilder<>(uid);
    }

    public <T> void processor(Class<? extends Processor> processorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, Processor.class)
                .addBinding(uid).to(processorClass);
    }

    /**
     * Returns the plugins name.
     */
    public final String getPluginName() {
        Objects.requireNonNull(pluginAnnotation);
        return pluginAnnotation.name();
    }

    /**
     * Returns the plugins scope.
     */
    public final PluginScope getPluginScope() {
        Objects.requireNonNull(pluginAnnotation);
        return pluginAnnotation.scope();
    }

    public class AggregatorBuilder<T> {

        private String uid;

        public AggregatorBuilder(String uid) {
            this.uid = uid;
        }

        public AggregatorBuilder<T> toReportData(String fileName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, ReportFilesNamesMap.class)
                    .addBinding(uid).toInstance(fileName);

            return this;
        }

        public AggregatorBuilder<T> toWidget(String widgetName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, WidgetsNamesMap.class)
                    .addBinding(uid).toInstance(widgetName);
            return this;
        }

        public AggregatorBuilder<T> toWidget(String widgetName, Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, Finalizer.class, WidgetDataFinalizer.class)
                    .addBinding(uid).to(finalizerClass);
            return toWidget(widgetName);
        }
    }
}
