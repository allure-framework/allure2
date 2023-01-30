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
package io.qameta.allure.core;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.exception.ContextNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Report configuration.
 *
 * @since 2.0
 */
public interface Configuration {

    /**
     * Returns all configured plugins.
     *
     * @return configured plugins.
     */
    List<Plugin> getPlugins();

    /**
     * Returns all configured aggregators.
     *
     * @return configured aggregators.
     */
    List<Aggregator> getAggregators();

    /**
     * Returns all configured readers.
     *
     * @return configured readers.
     */
    List<Reader> getReaders();

    /**
     * Resolve context by given type.
     *
     * @param contextType type of context to resolve.
     * @param <T>         the java type of context.
     * @return resolved context.
     */
    <T> Optional<T> getContext(Class<T> contextType);

    /**
     * The same as {@link #getContext(Class)} but throws an exception
     * if context doesn't present.
     *
     * @return resolved context.
     * @throws ContextNotFoundException if no such context present.
     */
    default <T> T requireContext(Class<T> contextType) {
        return getContext(contextType).orElseThrow(() -> new ContextNotFoundException(contextType));
    }
}
