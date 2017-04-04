package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.exception.ContextNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Configuration {

    List<PluginDescriptor> getPluginsDescriptors();

    List<Aggregator> getPlugins();

    List<Reader> getReaders();

    List<Widget> getWidgetPlugins();

    <T> Optional<T> getContext(Class<T> contextType);

    default <T> T requireContext(Class<T> contextType) {
        return getContext(contextType).orElseThrow(() -> new ContextNotFoundException(contextType));
    }
}
