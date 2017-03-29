package io.qameta.allure;

import io.qameta.allure.exception.ContextNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Configuration {

    List<PluginDescriptor> getPluginsDescriptors();

    List<Plugin> getPlugins();

    List<ResultsReader> getReaders();

    List<WidgetPlugin> getWidgetPlugins();

    <T> Optional<T> getContext(Class<T> contextType);

    default <T> T requireContext(Class<T> contextType) {
        return getContext(contextType).orElseThrow(() -> new ContextNotFoundException(contextType));
    }
}
