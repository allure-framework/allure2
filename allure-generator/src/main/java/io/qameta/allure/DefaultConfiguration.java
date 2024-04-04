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
package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of {@link Configuration}.
 *
 * @since 2.0
 */
public class DefaultConfiguration implements Configuration {

    private static final String UNDEFINED = "Undefined";

    private final List<Extension> extensions;

    private final List<Plugin> plugins;

    private final String uuid;

    private final String version;

    private final String reportName;

    private final String reportLanguage;

    /**
     * Instantiates a new Default configuration.
     *
     * @param extensions the extensions
     * @param plugins    the plugins
     * @deprecated use {@link ConfigurationBuilder} instead.
     */
    @Deprecated
    public DefaultConfiguration(final List<Extension> extensions,
                                final List<Plugin> plugins) {
        this(UUID.randomUUID().toString(), UNDEFINED, null, null, extensions, plugins);
    }

    /**
     * Instantiates a new Default configuration.
     *
     * @param reportName the report name
     * @param extensions the extensions
     * @param plugins    the plugins
     * @deprecated use {@link ConfigurationBuilder} instead.
     */
    @Deprecated
    public DefaultConfiguration(final String reportName,
                                final List<Extension> extensions,
                                final List<Plugin> plugins) {
        this(UUID.randomUUID().toString(), UNDEFINED, reportName, null, extensions, plugins);

    }

    /**
     * Instantiates a new Default configuration.
     *
     * @param uuid           the report uuid
     * @param version        the Allure version
     * @param reportName     the report name
     * @param reportLanguage the report language
     * @param extensions     the extensions
     * @param plugins        the plugins
     */
    DefaultConfiguration(final String uuid,
                         final String version,
                         final String reportName,
                         final String reportLanguage,
                         final List<Extension> extensions,
                         final List<Plugin> plugins) {
        this.reportName = reportName;
        this.reportLanguage = reportLanguage;
        this.extensions = extensions;
        this.plugins = plugins;
        this.uuid = uuid;
        this.version = version;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getReportLanguage() {
        return reportLanguage;
    }

    @Override
    public String getReportName() {
        return reportName;
    }

    @Override
    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    @Override
    public List<Extension> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

    @Override
    public <S, T extends Context<S>> Optional<T> getContext(final Class<T> contextType) {
        return getExtensions(contextType).stream()
                .findFirst();
    }

}
