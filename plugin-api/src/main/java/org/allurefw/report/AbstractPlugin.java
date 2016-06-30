package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Dmitry Baev baev@qameta.io
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

    public void processor(Class<? extends Processor> processorClass) {
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

    public final boolean hasStaticContent() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resource = loader.getResource("allure" + getPluginName() + "/index.js");
        return Objects.nonNull(resource);
    }

    public class AggregatorBuilder<T> {

        private String uid;

        public AggregatorBuilder(String uid) {
            this.uid = uid;
        }

        public AggregatorBuilder<T> toReportData(String fileName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, DataNamesMap.class)
                    .addBinding(uid).toInstance(fileName);
            return this;
        }

        public AggregatorBuilder<T> toReportData(String fileName, Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, Finalizer.class)
                    .addBinding(fileName).to(finalizerClass);
            return toReportData(fileName);
        }

        public AggregatorBuilder<T> toWidget(String widgetName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, WidgetsNamesMap.class)
                    .addBinding(uid).toInstance(widgetName);
            return this;
        }

        public AggregatorBuilder<T> toWidget(String widgetName, Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, Finalizer.class)
                    .addBinding(widgetName).to(finalizerClass);
            return toWidget(widgetName);
        }
    }
}
