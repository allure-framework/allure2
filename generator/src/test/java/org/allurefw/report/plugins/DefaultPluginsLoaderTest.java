package org.allurefw.report.plugins;

import com.google.inject.Guice;
import com.google.inject.Module;
import org.allurefw.report.ParentModule;
import org.allurefw.report.Plugin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static ru.yandex.qatools.matchers.nio.PathMatchers.exists;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultPluginsLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldLoad() throws Exception {
        Path pluginsDirectory = getPluginsDirectory();
        DefaultPluginsLoader pluginsLoader = new DefaultPluginsLoader(pluginsDirectory, folder.newFolder().toPath());
        List<Plugin> plugins = pluginsLoader.loadPlugins();
        assertThat(plugins, notNullValue());
        assertThat(plugins, hasSize(1));
        assertThat(plugins, hasItem(allOf(
                hasProperty("descriptor", allOf(
                        hasProperty("name", equalTo("xunit-plugin")),
                        hasProperty("moduleClass", equalTo("org.allurefw.report.xunit.XunitPlugin"))
                )),
                hasProperty("module", notNullValue()),
                hasProperty("archive", exists())
        )));
    }

    @Test
    public void shouldNotFailIfPluginDirectoryDoesNotExists() throws Exception {
        Path pluginsDirectory = folder.newFolder().toPath().resolve("pluginsDirectory");
        DefaultPluginsLoader loader = new DefaultPluginsLoader(pluginsDirectory, folder.newFolder().toPath());
        List<Plugin> plugins = loader.loadPlugins();
        assertThat(plugins, notNullValue());
        assertThat(plugins, empty());
    }

    @Test
    public void shouldNotFailIfWorkDirectoryDoesNotExists() throws Exception {
        Path pluginsDirectory = getPluginsDirectory();
        Path workDirectory = folder.newFolder().toPath().resolve("workDirectory");
        DefaultPluginsLoader loader = new DefaultPluginsLoader(pluginsDirectory, workDirectory);
        List<Plugin> plugins = loader.loadPlugins();
        assertThat(plugins, notNullValue());
        assertThat(plugins, hasSize(1));
        assertThat(plugins, hasItem(allOf(
                hasProperty("descriptor", allOf(
                        hasProperty("name", equalTo("xunit-plugin")),
                        hasProperty("moduleClass", equalTo("org.allurefw.report.xunit.XunitPlugin"))
                )),
                hasProperty("module", notNullValue()),
                hasProperty("archive", exists())
        )));
    }

    @Test
    public void shouldCreateInjector() throws Exception {
        Path pluginsDirectory = getPluginsDirectory();
        DefaultPluginsLoader pluginsLoader = new DefaultPluginsLoader(pluginsDirectory, folder.newFolder().toPath());
        List<Plugin> plugins = pluginsLoader.loadPlugins();
        List<Module> modules = plugins.stream()
                .map(Plugin::getModule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        ParentModule parentModule = new ParentModule(
                Collections.emptyList(),
                modules
        );
        Guice.createInjector(parentModule);
    }

    private Path getPluginsDirectory() throws IOException {
        Path dir = folder.newFolder().toPath();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dummy-plugin.zip")) {
            Files.copy(is, dir.resolve("dummy-plugin.zip"));
        }
        return dir;
    }
}
