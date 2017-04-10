package io.qameta.allure.plugin;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Extension;
import io.qameta.allure.core.Plugin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectoryPluginLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DefaultPluginLoader pluginLoader;

    @Before
    public void setUp() throws Exception {
        pluginLoader = new DefaultPluginLoader();
    }

    @Test
    public void shouldNotFailWhenPluginDirectoryNotExists() throws Exception {
        final Path pluginFolder = folder.newFolder().toPath().resolve("plugin");
        final Optional<Plugin> plugin = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);

        assertThat(plugin)
                .isEmpty();
    }

    @Test
    public void shouldLoadEmptyPlugin() throws Exception {
        final Path pluginDirectory = folder.newFolder().toPath();
        final Optional<Plugin> plugin = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginDirectory);

        assertThat(plugin)
                .isEmpty();
    }

    @Test
    public void shouldLoadPluginExtensions() throws Exception {
        final Path pluginFolder = folder.newFolder().toPath();
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
    public void shouldLoadStaticOnlyPlugin() throws Exception {
        final Path pluginFolder = folder.newFolder().toPath();
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

        final Path unpack = folder.newFolder().toPath();
        plugin.unpackReportStatic(unpack);

        assertThat(unpack.resolve("some-file"))
                .isRegularFile()
                .hasContent("ho-ho-ho");
    }

    @Test
    public void shouldLoadJarsInLibDirectory() throws Exception {
        final Path pluginFolder = folder.newFolder().toPath();
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
    public void shouldProcessInvalidConfigFile() throws Exception {
        final Path pluginFolder = folder.newFolder().toPath();
        add(pluginFolder, "static-file.txt", "allure-plugin.yml");

        final Optional<Plugin> loaded = pluginLoader.loadPlugin(getClass().getClassLoader(), pluginFolder);

        assertThat(loaded)
                .isEmpty();
    }

    private void add(final Path pluginDirectory, final String resourceName, final String dest) throws IOException {
        final Path destFile = pluginDirectory.resolve(dest);
        Files.createDirectories(destFile.getParent());
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, destFile);
        }
    }
}