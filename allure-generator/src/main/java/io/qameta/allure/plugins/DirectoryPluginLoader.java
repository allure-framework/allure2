package io.qameta.allure.plugins;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class DirectoryPluginLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryPluginLoader.class);

    public Optional<Plugin> loadPlugin(final ClassLoader parent, final Path pluginDirectory) {
        final Optional<PluginConfiguration> pluginConfiguration = loadPluginConfiguration(pluginDirectory);
        if (pluginConfiguration.isPresent()) {
            final PluginConfiguration configuration = pluginConfiguration.get();
            if (!configuration.getExtensions().isEmpty()) {
                final Optional<ClassLoader> loader = createClassLoader(parent, pluginDirectory);
                if (!loader.isPresent()) {
                    return Optional.empty();
                }

                final ClassLoader classLoader = loader.get();
                final List<Extension> extensions = configuration.getExtensions().stream()
                        .map(name -> load(classLoader, name))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                return Optional.of(new DefaultPlugin(configuration, extensions, pluginDirectory));
            }
        }
        return Optional.empty();
    }

    private Optional<Extension> load(final ClassLoader classLoader, final String name) {
        try {
            final Extension loaded = Extension.class.cast(
                    classLoader.loadClass(name).newInstance()
            );
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
            LOGGER.error("Could not read plugin configuration: {}", e);
            return Optional.empty();
        }
    }

    private Optional<ClassLoader> createClassLoader(final ClassLoader parent, final Path pluginDirectory) {
        final Path pluginJar = pluginDirectory.resolve("plugin.jar");
        if (Files.notExists(pluginJar)) {
            LOGGER.error("Could not find plugin.jar in directory {}", pluginDirectory);
            return Optional.empty();
        }

        try {
            final URL url = pluginJar.toUri().toURL();
            return Optional.of(new URLClassLoader(new URL[]{url}, parent));
        } catch (MalformedURLException e) {
            LOGGER.error("Could not load plugin.jar in directory {}: {}", pluginDirectory, e);
            return Optional.empty();
        }
    }
}
