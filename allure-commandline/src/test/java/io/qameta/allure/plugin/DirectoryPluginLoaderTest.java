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

import io.qameta.allure.Aggregator;
import io.qameta.allure.Extension;
import io.qameta.allure.core.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryPluginLoaderTest {

    private DefaultPluginLoader pluginLoader;

    @BeforeEach
    void setUp() {
        pluginLoader = new DefaultPluginLoader();
    }

    @Test
    void shouldNotFailWhenPluginDirectoryNotExists(@TempDir final Path temp) {
        final Path pluginFolder = temp.resolve("plugin");
        final Optional<Plugin> plugin = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);

        assertThat(plugin)
                .isEmpty();
    }

    @Test
    void shouldLoadEmptyPlugin(@TempDir final Path pluginDirectory) {
        final Optional<Plugin> plugin = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginDirectory);

        assertThat(plugin)
                .isEmpty();
    }

    @Test
    void shouldLoadPluginExtensions(@TempDir final Path pluginFolder) throws Exception {
        add(pluginFolder, "plugin.jar", "plugin.jar");
        add(pluginFolder, "dummy-plugin.yml", "allure-plugin.yml");

        final Optional<Plugin> loaded = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);

        assertThat(loaded)
                .isPresent();

        final Plugin plugin = loaded.get();
        assertThat(plugin.getConfig())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", "packages")
                .hasFieldOrPropertyWithValue("name", "Packages aggregator")
                .hasFieldOrPropertyWithValue("description", "The aggregator adds packages tab to the report");

        assertThat(plugin.getExtensions())
                .isNotNull()
                .hasSize(1);

        final Extension extension = plugin.getExtensions().get(0);
        assertThat(extension)
                .isNotNull()
                .isInstanceOf(Aggregator.class);

        assertThat(extension.getClass().getCanonicalName())
                .isEqualTo("io.qameta.allure.packages.PackagesPlugin");

    }

    @Test
    void shouldLoadStaticOnlyPlugin(@TempDir final Path temp) throws Exception {
        final Path pluginFolder = Files.createDirectories(temp.resolve("plugins"));
        add(pluginFolder, "static-file.txt", "static/some-file");
        add(pluginFolder, "dummy-plugin2.yml", "allure-plugin.yml");

        final Optional<Plugin> loaded = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);

        assertThat(loaded)
                .isPresent();

        final Plugin plugin = loaded.get();
        assertThat(plugin.getConfig())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", "dummy");

        assertThat(plugin.getExtensions())
                .isNotNull()
                .isEmpty();

        final Path unpack = Files.createDirectories(temp.resolve("unpack"));
        plugin.unpackReportStatic(unpack);

        assertThat(unpack.resolve("some-file"))
                .isRegularFile()
                .hasContent("ho-ho-ho");
    }

    @Test
    void shouldLoadJarsInLibDirectory(@TempDir final Path pluginFolder) throws Exception {
        add(pluginFolder, "plugin.jar", "lib/plugin.jar");
        add(pluginFolder, "dummy-plugin.yml", "allure-plugin.yml");

        final Optional<Plugin> loaded = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);

        assertThat(loaded)
                .isPresent();

        final Plugin plugin = loaded.get();
        assertThat(plugin.getConfig())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", "packages")
                .hasFieldOrPropertyWithValue("name", "Packages aggregator")
                .hasFieldOrPropertyWithValue("description", "The aggregator adds packages tab to the report");

        assertThat(plugin.getExtensions())
                .isNotNull()
                .hasSize(1);

        final Extension extension = plugin.getExtensions().get(0);
        assertThat(extension)
                .isNotNull()
                .isInstanceOf(Aggregator.class);

        assertThat(extension.getClass().getCanonicalName())
                .isEqualTo("io.qameta.allure.packages.PackagesPlugin");
    }

    @Test
    void shouldProcessInvalidConfigFile(@TempDir final Path pluginFolder) throws Exception {
        add(pluginFolder, "static-file.txt", "allure-plugin.yml");

        final Optional<Plugin> loaded = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);

        assertThat(loaded)
                .isEmpty();
    }

    private void add(final Path pluginDirectory, final String resourceName, final String dest) throws IOException {
        final Path destFile = pluginDirectory.resolve(dest);
        Files.createDirectories(destFile.getParent());
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(Objects.requireNonNull(is), destFile);
        }
    }
}
