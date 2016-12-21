package io.qameta.allure.plugins;

import com.google.inject.Guice;
import com.google.inject.Module;
import io.qameta.allure.ParentModule;
import io.qameta.allure.Plugin;
import io.qameta.allure.PluginDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
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
public class DefaultPluginLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldLoad() throws Exception {
        Path pluginsDirectory = getPluginsDirectory();
        DefaultPluginLoader pluginsLoader = new DefaultPluginLoader(pluginsDirectory);
        List<Plugin> plugins = pluginsLoader.loadPlugins(Collections.singleton("behaviors-plugin"));
        assertThat(plugins, notNullValue());
        assertThat(plugins, hasSize(1));
        assertThat(plugins, hasItem(allOf(
                hasProperty("descriptor", allOf(
                        hasProperty("name", equalTo("behaviors-plugin")),
                        hasProperty("moduleClass", equalTo("io.qameta.allure.behaviors.BehaviorsPlugin"))
                )),
                hasProperty("module", notNullValue()),
                hasProperty("enabled", equalTo(true)),
                hasProperty("pluginDirectory", exists())
        )));
    }

    @Test
    public void shouldLoadPluginDescriptor() throws Exception {
        Path archive = getPluginDirectory();
        Optional<PluginDescriptor> plugin = DefaultPluginLoader.readPluginDescriptor(archive);
        assertThat(plugin, isPresent());
        assertThat(plugin, hasValue(allOf(
                hasProperty("name", equalTo("behaviors-plugin")),
                hasProperty("moduleClass", equalTo("io.qameta.allure.behaviors.BehaviorsPlugin"))
        )));
    }

    @Test
    public void shouldNotFailIfPluginDirectoryDoesNotExists() throws Exception {
        Path pluginsDirectory = folder.newFolder().toPath().resolve("pluginsDirectory");
        DefaultPluginLoader loader = new DefaultPluginLoader(pluginsDirectory);
        List<Plugin> plugins = loader.loadPlugins(Collections.emptySet());
        assertThat(plugins, notNullValue());
        assertThat(plugins, empty());
    }

    @Test
    public void shouldCreateInjector() throws Exception {
        Path pluginsDirectory = getPluginsDirectory();
        DefaultPluginLoader pluginsLoader = new DefaultPluginLoader(pluginsDirectory);
        List<Plugin> plugins = pluginsLoader.loadPlugins(Collections.emptySet());
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
            ZipUtil.unwrap(is, dir.resolve("dummy-plugin").toFile());
        }
        return dir;
    }

    private Path getPluginDirectory() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dummy-plugin.zip")) {
            Path pluginsDirectory = folder.newFolder().toPath();
            ZipUtil.unwrap(is, pluginsDirectory.toFile());
            return pluginsDirectory;
        }
    }
}
