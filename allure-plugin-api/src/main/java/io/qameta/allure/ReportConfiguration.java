package io.qameta.allure;

import io.qameta.allure.exception.ContextNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public interface ReportConfiguration {

    List<Plugin> getPlugins();

    List<Processor> getProcessors();

    List<Aggregator> getAggregators();

    List<ResultsReader> getReaders();

    List<WidgetAggregator> getWidgetAggregators();

    <T> Optional<T> getContext(Class<T> contextType);

    default <T> T requireContext(Class<T> contextType) {
        return getContext(contextType).orElseThrow(() -> new ContextNotFoundException(contextType));
    }
}
