package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public abstract class AbstractPlugin extends AbstractModule {

    private final Plugin pluginAnnotation = getClass().getAnnotation(Plugin.class);

    /**
     * Register the test case aggregator.
     */
    protected final <T> void aggregator(T identity, Aggregator<T> aggregator) {
        aggregator(identity).toProvider(() -> aggregator);
    }

    /**
     * Get the aggregator binder.
     */
    protected <T> LinkedBindingBuilder<Aggregator> aggregator(T identity) {
        return MapBinder.newMapBinder(binder(), Object.class, Aggregator.class)
                .addBinding(identity);
    }

    /**
     * Shortcut for {@link #widgetData(String, Object, Function)}
     */
    protected <T extends Serializable> void widgetData(T identity) {
        widgetData(identity, Function.identity());
    }

    /**
     * Shortcut for {@link #widgetData(String, Object, Function)}
     */
    protected <T> void widgetData(T identity, Function<T, Serializable> function) {
        widgetData(getPluginName(), identity, function);
    }

    /**
     * Add the widget data.
     */
    protected <T> void widgetData(String widgetName, T identity, Function<T, Serializable> function) {
        widgetDataBuilder(widgetName).toProvider(() -> function.apply(identity));
    }

    /**
     * Add the widget data.
     */
    protected <T> LinkedBindingBuilder<Object> widgetDataBuilder(String widgetName) {
        return MapBinder.newMapBinder(binder(), String.class, Object.class, WidgetData.class)
                .addBinding(widgetName);
    }

    /**
     * Shortcut for {@link #reportData(Object, Function)}
     */
    protected <T extends Serializable> void reportData(T identity) {
        reportData(identity, Function.identity());
    }

    /**
     * Shortcut for {@link #reportData(String, Object, Function)}
     */
    protected <T> void reportData(T identity, Function<T, Serializable> function) {
        reportData(getFileName(), identity, function);
    }

    /**
     * Add the report data.
     */
    protected <T> void reportData(String fileName, T identity, Function<T, Serializable> function) {
        reportDataBuilder(fileName).toProvider(() -> function.apply(identity));
    }

    /**
     * Add the report data.
     */
    protected <T> LinkedBindingBuilder<Object> reportDataBuilder(String fileName) {
        return MapBinder.newMapBinder(binder(), String.class, Object.class, ReportData.class)
                .addBinding(fileName);
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
