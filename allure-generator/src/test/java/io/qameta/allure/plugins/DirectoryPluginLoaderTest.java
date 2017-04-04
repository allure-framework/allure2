package io.qameta.allure.plugins;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Plugin;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class DirectoryPluginLoaderTest {

    @Test
    @Ignore
    public void shouldLoad() throws Exception {
        final DirectoryPluginLoader pluginLoader = new DirectoryPluginLoader();
        final Path directory = Paths.get("/Users/charlie/projects/allure2/allure-commandline/build/plugins/behaviors-plugin-2.0-SNAPSHOT");
        final Optional<Plugin> plugin = pluginLoader.loadPlugin(DirectoryPluginLoader.class.getClassLoader(), directory);
        assertThat(plugin)
                .isPresent();

        final Plugin loaded = plugin.get();

        assertThat(loaded.getAggregators())
                .hasSize(1);

        final Aggregator aggregator = loaded.getAggregators().get(0);
    }
}