package io.qameta.allure.plugins;

import com.google.inject.Module;
import io.qameta.allure.Plugin;
import io.qameta.allure.PluginDescriptor;
import io.qameta.allure.PluginsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultPluginLoader implements PluginsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPluginLoader.class);

    public static final String DESCRIPTOR_ENTRY_NAME = "plugin-descriptor.xml";
    public static final String PLUGIN_JAR_ENTRY_NAME = "plugin.jar";
    public static final String LIB_DIR_NAME = "lib";

    private final Path pluginsDirectory;

    public DefaultPluginLoader(Path pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;
    }

    @Override
    public List<Plugin> loadPlugins(Set<String> enabledPlugins) {
        if (!Files.exists(pluginsDirectory)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.walk(pluginsDirectory)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(DefaultPluginLoader::isPluginDirectory)
                    .map(directory -> loadPlugin(directory, enabledPlugins))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error loading plugins", e);
            return Collections.emptyList();
        }
    }

    private Optional<Plugin> loadPlugin(Path pluginDirectory, Set<String> enabledPlugins) {
        Optional<PluginDescriptor> pluginDescriptor = readPluginDescriptor(pluginDirectory);
        if (!pluginDescriptor.isPresent()) {
            return Optional.empty();
        }
        PluginDescriptor descriptor = pluginDescriptor.get();
        Plugin plugin = loadPluginModule(pluginDirectory, descriptor)
                .map(module -> new Plugin(descriptor, module, pluginDirectory, isEnabled(descriptor, enabledPlugins)))
                .orElseGet(() -> new Plugin(descriptor, null, pluginDirectory, isEnabled(descriptor, enabledPlugins)));
        return Optional.of(plugin);
    }

    private Optional<Module> loadPluginModule(Path pluginDirectory, PluginDescriptor descriptor) {
        String moduleClass = descriptor.getModuleClass();
        if (Objects.isNull(moduleClass) || moduleClass.isEmpty()) {
            return Optional.empty();
        }
        try {
            ClassLoader parent = getClass().getClassLoader();
            //We should not close this classloader in order to load other plugin classes.
            URLClassLoader classLoader = new URLClassLoader(getClassPath(pluginDirectory), parent);
            return Optional.of((Module) classLoader.loadClass(moduleClass).newInstance());
        } catch (Exception e) {
            LOGGER.error("Could not load module {} for plugin {} {}", moduleClass, descriptor.getName(), e);
            return Optional.empty();
        }
    }

    private URL[] getClassPath(Path pluginDirectory) throws IOException {
        Path lib = pluginDirectory.resolve(LIB_DIR_NAME);
        Stream<Path> libs = Files.isDirectory(lib) ? getChildren(lib) : Stream.empty();
        Stream<Path> pluginJar = Stream.of(pluginDirectory.resolve(PLUGIN_JAR_ENTRY_NAME));
        return Stream.concat(pluginJar, libs)
                .map(this::toUrl)
                .toArray(URL[]::new);
    }

    private Stream<Path> getChildren(Path directory) throws IOException {
        try (Stream<Path> children = Files.walk(directory, 1, FileVisitOption.FOLLOW_LINKS)) {
            //we need to load lazy directory stream to memory and close it
            return children.collect(Collectors.toList()).stream();
        }
    }

    public static Optional<PluginDescriptor> readPluginDescriptor(Path pluginDirectory) {
        Path descriptor = pluginDirectory.resolve(DESCRIPTOR_ENTRY_NAME);
        try (InputStream is = Files.newInputStream(descriptor)) {
            return Optional.of(JAXB.unmarshal(is, PluginDescriptor.class));
        } catch (IOException e) {
            LOGGER.error("Could not read plugin descriptor {} {}", pluginDirectory.getFileName().toString(), e);
            return Optional.empty();
        }
    }

    public static boolean isPluginDirectory(Path directory) {
        return Files.exists(directory.resolve(DESCRIPTOR_ENTRY_NAME));
    }

    private boolean isEnabled(PluginDescriptor descriptor, Set<String> enabledPlugins) {
        return Objects.isNull(enabledPlugins) || enabledPlugins.contains(descriptor.getName());
    }

    private URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid plugin", e);
        }
    }
}
