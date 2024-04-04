/*
 *  Copyright 2016-2024 Qameta Software Inc
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
import io.qameta.allure.Context;
import io.qameta.allure.Extension;
import io.qameta.allure.Reader;
import io.qameta.allure.exception.ContextNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Report configuration.
 *
 * @since 2.0
 */
public interface Configuration {

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    default String getUuid() {
        return null;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    default String getVersion() {
        return null;
    }

    /**
     * Returns the report language. If not specified, uses "en".
     *
     * @return the report language.
     */
    default String getReportLanguage() {
        return null;
    }

    /**
     * Returns the report name.
     *
     * @return the report name.
     */
    default String getReportName() {
        return null;
    }

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
     * @deprecated for removal. Use {@link #getExtensions()} instead.
     */
    @Deprecated
    default List<Aggregator> getAggregators() {
        return getExtensions(Aggregator.class);
    }

    /**
     * Returns all configured readers.
     *
     * @return configured readers.
     * @deprecated for removal. Use {@link #getExtensions()} instead.
     */
    @Deprecated
    default List<Reader> getReaders() {
        return getExtensions(Reader.class);
    }

    /**
     * Returns all discovered extensions.
     *
     * @return configured extensions.
     */
    List<Extension> getExtensions();

    /**
     * Returns all discovered extensions of specified type.
     *
     * @param <T>           the type of extension.
     * @param extensionType the type of extension.
     * @return configured extensions.
     */
    default <T extends Extension> List<T> getExtensions(final Class<T> extensionType) {
        return getExtensions().stream()
                .filter(extensionType::isInstance)
                .map(extensionType::cast)
                .collect(Collectors.toList());
    }

    /**
     * Resolve context by given type.
     *
     * @param <T>         the java type of context's type.
     * @param <S>         the java type of context.
     * @param contextType type of context to resolve.
     * @return resolved context.
     */
    default <T, S extends Context<T>> Optional<S> getContext(final Class<S> contextType) {
        return getExtensions(contextType).stream()
                .findFirst();
    }

    /**
     * The same as {@link #getContext(Class)} but throws an exception
     * if context doesn't present.
     *
     * @param <T>         the type parameter
     * @param <S>         the type parameter
     * @param contextType the context type
     * @return resolved context.
     * @throws ContextNotFoundException if no such context present.
     */
    default <T, S extends Context<T>> S requireContext(final Class<S> contextType) {
        return getContext(contextType).orElseThrow(() -> new ContextNotFoundException(contextType));
    }
}
