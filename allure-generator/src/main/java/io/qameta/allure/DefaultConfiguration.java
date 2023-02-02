/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
    public <T> Optional<T> getContext(final Class<T> contextType) {
        return extensions.stream()
                .filter(contextType::isInstance)
                .map(contextType::cast)
                .findFirst();
    }
}
