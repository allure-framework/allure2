/*
 *  Copyright 2016-2026 Qameta Software Inc
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
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Extension;
import io.qameta.allure.core.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryPluginLoaderTest {

    private DefaultPluginLoader pluginLoader;

    @BeforeEach
    void setUp() {
        pluginLoader = new DefaultPluginLoader();
    }

    /**
     * Verifies loading a plugin from a missing directory is treated as absent.
     * The test checks the loader returns an empty optional instead of failing.
     */
    @Description
    @Test
    void shouldNotFailWhenPluginDirectoryNotExists(@TempDir final Path temp) {
        final Path pluginFolder = temp.resolve("plugin");
        final Optional<Plugin> plugin = loadPlugin(pluginFolder);

        assertThat(plugin)
                .isEmpty();
    }

    /**
     * Verifies an empty plugin directory does not produce a plugin.
     * The test checks the loader returns an empty optional when no descriptor is present.
     */
    @Description
    @Test
    void shouldLoadEmptyPlugin(@TempDir final Path pluginDirectory) {
        final Optional<Plugin> plugin = loadPlugin(pluginDirectory);

        assertThat(plugin)
                .isEmpty();
    }

    /**
     * Verifies loading a plugin descriptor and extension jar from the plugin root.
     * The test checks plugin metadata and the aggregator extension class are discovered.
     */
    @SuppressWarnings("deprecation")
    @Description
    @Test
    void shouldLoadPluginExtensions(@TempDir final Path pluginFolder) throws Exception {
        add(pluginFolder, "plugin.jar", "plugin.jar");
        add(pluginFolder, "dummy-plugin.yml", "allure-plugin.yml");

        final Optional<Plugin> loaded = loadPlugin(pluginFolder);

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

    /**
     * Verifies a plugin with only static files is loaded without extensions.
     * The test checks metadata and copied static file content are available from the plugin model.
     */
    @Description
    @Test
    void shouldLoadStaticOnlyPlugin(@TempDir final Path temp) throws Exception {
        final Path pluginFolder = Files.createDirectories(temp.resolve("plugins"));
        add(pluginFolder, "static-file.txt", "static/some-file");
        add(pluginFolder, "dummy-plugin2.yml", "allure-plugin.yml");

        final Optional<Plugin> loaded = loadPlugin(pluginFolder);

        assertThat(loaded)
                .isPresent();

        final Plugin plugin = loaded.get();
        assertThat(plugin.getConfig())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", "dummy");

        assertThat(plugin.getExtensions())
                .isNotNull()
                .isEmpty();


        final Map<String, Path> pluginFiles = plugin.getPluginFiles();

        assertThat(pluginFiles).containsKey("some-file");
        assertThat(pluginFiles.get("some-file"))
                .isRegularFile()
                .hasContent("ho-ho-ho");
    }

    /**
     * Verifies extension jars are discovered from a plugin lib directory.
     * The test checks plugin metadata and the aggregator extension class are loaded from lib.
     */
    @SuppressWarnings("deprecation")
    @Description
    @Test
    void shouldLoadJarsInLibDirectory(@TempDir final Path pluginFolder) throws Exception {
        add(pluginFolder, "plugin.jar", "lib/plugin.jar");
        add(pluginFolder, "dummy-plugin.yml", "allure-plugin.yml");

        final Optional<Plugin> loaded = loadPlugin(pluginFolder);

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

    /**
     * Verifies invalid plugin descriptors are ignored.
     * The test checks a malformed descriptor yields an empty plugin result.
     */
    @Description
    @Test
    void shouldProcessInvalidConfigFile(@TempDir final Path pluginFolder) throws Exception {
        add(pluginFolder, "static-file.txt", "allure-plugin.yml");

        final Optional<Plugin> loaded = loadPlugin(pluginFolder);

        assertThat(loaded)
                .isEmpty();
    }

    private Optional<Plugin> loadPlugin(final Path pluginFolder) {
        return Allure.step("Load plugin from directory", () -> {
            final Optional<Plugin> plugin = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);
            Allure.addAttachment("loaded-plugin.txt", "text/plain", describePlugin(plugin));
            return plugin;
        });
    }

    private void add(final Path pluginDirectory, final String resourceName, final String dest) throws IOException {
        Allure.step("Copy plugin resource " + resourceName + " as " + dest, () -> {
            final Path destFile = pluginDirectory.resolve(dest);
            Files.createDirectories(destFile.getParent());
            final byte[] content;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                content = Objects.requireNonNull(is).readAllBytes();
            }
            Files.write(destFile, content);
            attachCopiedResource(dest, content);
        });
    }

    private void attachCopiedResource(final String fileName, final byte[] content) {
        if (isText(fileName)) {
            Allure.addAttachment(
                    Path.of(fileName).getFileName().toString(),
                    contentType(fileName),
                    new String(content, StandardCharsets.UTF_8),
                    extension(fileName)
            );
            return;
        }
        Allure.addAttachment(
                Path.of(fileName).getFileName() + ".metadata.txt",
                "text/plain",
                "fileName=" + Path.of(fileName).getFileName() + System.lineSeparator()
                        + "size=" + content.length
        );
    }

    private String describePlugin(final Optional<Plugin> plugin) {
        if (plugin.isEmpty()) {
            return "plugin=<empty>";
        }
        final Plugin value = plugin.get();
        return String.format(
                "id=%s%nname=%s%ndescription=%s%nextensions=%s%npluginFiles=%s",
                value.getConfig().getId(),
                value.getConfig().getName(),
                value.getConfig().getDescription(),
                value.getExtensions().stream()
                        .map(extension -> extension.getClass().getCanonicalName())
                        .collect(Collectors.joining(", ")),
                value.getPluginFiles().keySet().stream()
                        .sorted()
                        .collect(Collectors.joining(", "))
        );
    }

    private boolean isText(final String fileName) {
        return fileName.endsWith(".yml") || fileName.endsWith(".txt") || !fileName.contains(".");
    }

    private String contentType(final String fileName) {
        return fileName.endsWith(".yml") ? "application/yaml" : "text/plain";
    }

    private String extension(final String fileName) {
        return fileName.endsWith(".yml") ? ".yml" : ".txt";
    }
}
