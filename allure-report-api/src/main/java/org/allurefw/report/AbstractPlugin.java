package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.MapBinder;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public abstract class AbstractPlugin extends AbstractModule {

    private final Plugin pluginAnnotation = getClass().getAnnotation(Plugin.class);

    public BindBuilder use(Class<?> clazz) {
        return new BindBuilder(clazz);
    }

    public class BindBuilder {

        private Class<?> clazz;

        public BindBuilder(Class<?> clazz) {
            this.clazz = clazz;
        }

        public void asSource() {
        }

        public AggregatorBuilder asAggregator() {
            //noinspection unchecked
            Class<? extends Aggregator> aggregatorClass = (Class<? extends Aggregator>) this.clazz;
            bind(Aggregator.class).to(aggregatorClass);
            String uid = UUID.randomUUID().toString();
            MapBinder.newMapBinder(binder(), String.class, Aggregator.class)
                    .addBinding(uid).to(aggregatorClass);

            return new AggregatorBuilder(aggregatorClass, uid);
        }

        public ProcessorBuilder asProcessor() {
            //noinspection unchecked
            Class<? extends Processor> processorClass = (Class<? extends Processor>) this.clazz;
            bind(Processor.class).to(processorClass);
            return new ProcessorBuilder();
        }

    }

    public class AggregatorBuilder {

        private Class<? extends Aggregator> clazz;

        private String uid;

        public AggregatorBuilder(Class<? extends Aggregator> clazz, String uid) {
            this.clazz = clazz;
            this.uid = uid;
        }

        public AggregatorBuilder toReportData(String fileName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, FileNamesMap.class)
                    .addBinding(uid).toInstance(fileName);

            return this;
        }

        public AggregatorBuilder toWidget(String widgetName) {
            return this;
        }

        public AggregatorBuilder to(Class<? extends Endpoint> endpoint) {
            return this;
        }

    }

    public class ProcessorBuilder {

        public ProcessorBuilder toReportData(String fileName) {
            return this;
        }

        public ProcessorBuilder toWidget(String widgetName) {
            return this;
        }

        public ProcessorBuilder to(Class<? extends Endpoint> endpoint) {
            return this;
        }

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
}
