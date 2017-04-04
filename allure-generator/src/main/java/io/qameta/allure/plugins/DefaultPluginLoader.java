package io.qameta.allure.plugins;

import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.core.PluginDescriptor;
import io.qameta.allure.core.PluginsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultPluginLoader implements PluginsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPluginLoader.class);

    public static final String DESCRIPTOR_ENTRY_NAME = "plugin-descriptor.xml";

    private final Path pluginsDirectory;

    public DefaultPluginLoader(final Path pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;
    }

    @Override
    public List<PluginDescriptor> loadPlugins() {
        if (!Files.exists(pluginsDirectory)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.walk(pluginsDirectory)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(DefaultPluginLoader::isPluginDirectory)
                    .map(this::loadPlugin)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error loading plugins", e);
            return Collections.emptyList();
        }
    }

    private Optional<PluginDescriptor> loadPlugin(final Path pluginDirectory) {
        return readPluginConfiguration(pluginDirectory)
                .map(configuration -> new PluginDescriptor(configuration, pluginDirectory));
    }

    public static Optional<PluginConfiguration> readPluginConfiguration(final Path pluginDirectory) {
        final Path descriptor = pluginDirectory.resolve(DESCRIPTOR_ENTRY_NAME);
        try (InputStream is = Files.newInputStream(descriptor)) {
            return Optional.of(JAXB.unmarshal(is, PluginConfiguration.class));
        } catch (IOException e) {
            LOGGER.error("Could not read plugin descriptor {} {}", pluginDirectory.getFileName(), e);
            return Optional.empty();
        }
    }

    public static boolean isPluginDirectory(final Path directory) {
        return Files.exists(directory.resolve(DESCRIPTOR_ENTRY_NAME));
    }
}
