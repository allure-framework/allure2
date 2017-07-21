package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link Configuration}.
 *
 * @since 2.0
 */
public class DefaultConfiguration implements Configuration {

    private final List<Extension> extensions;

    private final List<Plugin> plugins;

    public DefaultConfiguration(final List<Extension> extensions,
                                final List<Plugin> plugins) {
        this.extensions = extensions;
        this.plugins = plugins;
    }

    @Override
    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    @Override
    public List<Aggregator> getAggregators() {
        return extensions.stream()
                .filter(Aggregator.class::isInstance)
                .map(Aggregator.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reader> getReaders() {
        return extensions.stream()
                .filter(Reader.class::isInstance)
                .map(Reader.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Widget> getWidgets() {
        return extensions.stream()
                .filter(Widget.class::isInstance)
                .map(Widget.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public <T> Optional<T> getContext(final Class<T> contextType) {
        return extensions.stream()
                .filter(contextType::isInstance)
                .map(contextType::cast)
                .findFirst();
    }
}
