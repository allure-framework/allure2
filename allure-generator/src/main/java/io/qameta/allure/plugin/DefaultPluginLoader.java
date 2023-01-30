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
package io.qameta.allure.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.qameta.allure.Extension;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.core.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Default plugin loader that load plugins from given directory.
 *
 * @since 2.0
 */
public class DefaultPluginLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPluginLoader.class);

    public Optional<Plugin> loadPlugin(final ClassLoader parent, final Path pluginDirectory) {
        final Optional<PluginConfiguration> pluginConfiguration = loadPluginConfiguration(pluginDirectory);
        if (pluginConfiguration.isPresent()) {
            final PluginConfiguration configuration = pluginConfiguration.get();
            if (!configuration.getExtensions().isEmpty()) {
                final ClassLoader classLoader = createClassLoader(parent, pluginDirectory);
                final List<Extension> extensions = configuration.getExtensions().stream()
                        .map(name -> load(classLoader, name))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                return Optional.of(new DefaultPlugin(configuration, extensions, pluginDirectory));
            } else {
                return Optional.of(new DefaultPlugin(configuration, Collections.emptyList(), pluginDirectory));
            }
        }
        return Optional.empty();
    }

    private Optional<Extension> load(final ClassLoader classLoader, final String name) {
        try {
            final Extension loaded = (Extension) classLoader.loadClass(name)
                    .getDeclaredConstructor().newInstance();
            return Optional.of(loaded);
        } catch (Exception e) {
            LOGGER.error("Could not load extension class {}: {}", name, e);
            return Optional.empty();
        }
    }

    private Optional<PluginConfiguration> loadPluginConfiguration(final Path pluginDirectory) {
        final Path configuration = pluginDirectory.resolve("allure-plugin.yml");
        if (Files.notExists(configuration)) {
            LOGGER.warn("Invalid plugin directory " + pluginDirectory);
            return Optional.empty();
        }

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = Files.newInputStream(configuration)) {
            return Optional.of(mapper.readValue(is, PluginConfiguration.class));
        } catch (IOException e) {
            LOGGER.error("Could not read plugin configuration", e);
            return Optional.empty();
        }
    }

    private ClassLoader createClassLoader(final ClassLoader parent, final Path pluginDirectory) {
        final Path lib = pluginDirectory.resolve("lib");
        final URL[] urls = Stream.of(pluginDirectory, lib)
                .filter(Files::isDirectory)
                .flatMap(dir -> jarsInDirectory(dir).stream())
                .toArray(URL[]::new);
        return new URLClassLoader(urls, parent);
    }

    private List<URL> jarsInDirectory(final Path directory) {
        final DirectoryStream.Filter<Path> pathFilter = entry ->
                Files.isRegularFile(entry) && entry.toString().endsWith(".jar");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, pathFilter)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .map(this::toUrlSafe)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Could not load plugin", e);
            return Collections.emptyList();
        }
    }

    private Optional<URL> toUrlSafe(final Path path) {
        try {
            return Optional.of(path.toUri().toURL());
        } catch (MalformedURLException e) {
            LOGGER.error("Could not load {}: {}", path, e);
            return Optional.empty();
        }
    }
}
