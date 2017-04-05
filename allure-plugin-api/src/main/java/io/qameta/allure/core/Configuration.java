package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.exception.ContextNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Report configuration.
 *
 * @since 2.0
 */
public interface Configuration {

    List<Plugin> getPlugins();

    List<Aggregator> getAggregators();

    List<Reader> getReaders();

    List<Widget> getWidgets();

    <T> Optional<T> getContext(Class<T> contextType);

    default <T> T requireContext(Class<T> contextType) {
        return getContext(contextType).orElseThrow(() -> new ContextNotFoundException(contextType));
    }
}
