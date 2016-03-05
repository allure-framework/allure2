package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public abstract class AbstractPlugin extends AbstractModule {

    private final Plugin pluginAnnotation = getClass().getAnnotation(Plugin.class);

    /**
     * Shortcut for {@link #aggregator(String, Serializable, Class)}.
     */
    public final <T extends Serializable> void aggregator(
            T identity, Class<? extends Aggregator<T>> aggregatorClass) {
        aggregator(getFileName(), identity, aggregatorClass);
    }

    /**
     * Register the test case aggregator.
     */
    public final <T extends Serializable> void aggregator(
            String fileName, T identity, Class<? extends Aggregator<T>> aggregatorClass) {
        aggregator(fileName, identity).to(aggregatorClass);
    }

    /**
     * Shortcut for {@link #aggregator(String, Serializable, Supplier)}
     */
    public final <T extends Serializable> void aggregator(
            T identity, Supplier<? extends Aggregator<T>> supplier) {
        aggregator(getFileName(), identity, supplier);
    }

    /**
     * Register the test case aggregator.
     */
    public final <T extends Serializable> void aggregator(
            String fileName, T identity, Supplier<? extends Aggregator<T>> supplier) {
        aggregator(fileName, identity).toProvider(supplier::get);
    }

    /**
     * Get the aggregator binder.
     */
    protected <T extends Serializable> LinkedBindingBuilder<Aggregator> aggregator(
            String fileName, T identity) {

        MapBinder.newMapBinder(binder(), String.class, ReportData.class)
                .addBinding(fileName).toInstance(() -> identity);

        return MapBinder.newMapBinder(binder(), Serializable.class, Aggregator.class)
                .addBinding(identity);
    }

    /**
     * Returns the default file name for test case aggregator.
     */
    protected String getFileName() {
        return getPluginName() + ".json";
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
