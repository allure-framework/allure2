package org.allurefw.report.plugins;

import com.google.inject.Module;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginDescriptor;
import org.allurefw.report.PluginsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultPluginsLoader implements PluginsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPluginsLoader.class);

    public static final String PLUGIN_FILE_SUFFIX = ".zip";
    public static final String DESCRIPTOR_ENTRY_NAME = "plugin-descriptor.xml";
    public static final String PLUGIN_JAR_ENTRY_NAME = "plugin.jar";

    private final Path pluginsDirectory;

    private final Path workDirectory;

    public DefaultPluginsLoader(Path pluginsDirectory, Path workDirectory) {
        this.pluginsDirectory = pluginsDirectory;
        this.workDirectory = workDirectory;
    }

    @Override
    public List<Plugin> loadPlugins() {
        if (!Files.exists(pluginsDirectory)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.walk(pluginsDirectory)) {
            Files.createDirectories(workDirectory);
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isZipArchive)
                    .filter(this::isPluginArchive)
                    .map(this::loadPlugin)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error loading plugins", e);
            return Collections.emptyList();
        }
    }

    private Optional<Plugin> loadPlugin(Path archive) {
        return open(archive, zipFile -> readPluginDescriptor(zipFile)
                .map(descriptor -> loadPluginModule(descriptor, zipFile)
                        .map(module -> new Plugin(descriptor, module, archive))
                        .orElseGet(() -> new Plugin(descriptor, null, archive))
                )
        );
    }

    private <T> Optional<T> open(Path zipArchive, Function<ZipFile, Optional<T>> function) {
        try (ZipFile zipFile = new ZipFile(zipArchive.toFile())) {
            return function.apply(zipFile);
        } catch (IOException e) {
            LOGGER.error("Could not open zip file {} {}", zipArchive, e);
            return Optional.empty();
        }
    }

    private Optional<PluginDescriptor> readPluginDescriptor(ZipFile zipFile) {
        return zipFile.stream()
                .filter(this::isPluginDescriptorEntry)
                .findAny()
                .map(entry -> readPluginDescriptor(zipFile, entry))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<PluginDescriptor> readPluginDescriptor(ZipFile zipFile, ZipEntry entry) {
        try (InputStream is = zipFile.getInputStream(entry)) {
            return Optional.of(JAXB.unmarshal(is, PluginDescriptor.class));
        } catch (IOException e) {
            LOGGER.error("Could not read plugin descriptor {} {}", zipFile.getName(), e);
            return Optional.empty();
        }
    }

    private Optional<Module> loadPluginModule(PluginDescriptor descriptor, ZipFile zipFile) {
        if (descriptor.getModuleClass() == null || descriptor.getModuleClass().isEmpty()) {
            return Optional.empty();
        }
        return zipFile.stream()
                .filter(this::isPluginJarEntry)
                .findAny()
                .map(entry -> loadPluginModule(descriptor, zipFile, entry))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<Module> loadPluginModule(PluginDescriptor descriptor, ZipFile zipFile, ZipEntry entry) {
        try (InputStream is = zipFile.getInputStream(entry)) {
            Path pluginJar = Files.createTempFile(workDirectory, descriptor.getName(), PLUGIN_JAR_ENTRY_NAME);
            Files.copy(is, pluginJar, StandardCopyOption.REPLACE_EXISTING);
            URL[] classPath = new URL[]{pluginJar.toUri().toURL()};
            ClassLoader parent = getClass().getClassLoader();
            //We should not close this classloader to load other plugin classes.
            URLClassLoader classLoader = new URLClassLoader(classPath, parent);
            return Optional.of((Module) classLoader.loadClass(descriptor.getModuleClass()).newInstance());
        } catch (Exception e) {
            LOGGER.error("Could not load module {} for plugin {} {}",
                    descriptor.getModuleClass(), descriptor.getName(), e);
            return Optional.empty();
        }
    }

    private boolean isPluginArchive(Path path) {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            return zipFile.stream()
                    .filter(this::isPluginDescriptorEntry)
                    .findAny()
                    .isPresent();
        } catch (IOException e) {
            LOGGER.debug("Could not process plugin archive {} {}", path, e);
            return false;
        }
    }

    private boolean isPluginDescriptorEntry(ZipEntry entry) {
        return DESCRIPTOR_ENTRY_NAME.equals(entry.getName());
    }

    private boolean isPluginJarEntry(ZipEntry entry) {
        return PLUGIN_JAR_ENTRY_NAME.equals(entry.getName());
    }

    private boolean isZipArchive(Path file) {
        return file.getFileName().toString().endsWith(PLUGIN_FILE_SUFFIX);
    }
}
