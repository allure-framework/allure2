package org.allurefw.report;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.allurefw.report.plugins.DefaultPluginsLoader;
import org.allurefw.report.plugins.EmptyPluginsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author charlie (Dmitry Baev).
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final String STATIC_FILES_PREFIX = "static/";

    private final PluginsLoader pluginsLoader;

    private final Set<String> enabledPlugins;

    public Main() {
        this(new EmptyPluginsLoader(), Collections.emptySet());
    }

    public Main(Path pluginsDirectory, Path workDirectory, Set<String> enabledPlugins) {
        this(new DefaultPluginsLoader(pluginsDirectory, workDirectory), enabledPlugins);
    }

    public Main(PluginsLoader pluginsLoader, Set<String> enabledPlugins) {
        this.pluginsLoader = pluginsLoader;
        this.enabledPlugins = enabledPlugins;
    }

    public List<Plugin> loadPlugins() {
        return pluginsLoader.loadPlugins(enabledPlugins);
    }

    public ReportInfo createReport(Path... sources) {
        List<Plugin> plugins = pluginsLoader.loadPlugins(enabledPlugins);
        ReportFactory factory = createInjector(plugins)
                .getInstance(ReportFactory.class);
        return factory.create(sources);
    }

    public void generate(Path output, Path... sources) {
        List<Plugin> plugins = pluginsLoader.loadPlugins(enabledPlugins);
        Injector injector = createInjector(plugins);
        ReportFactory factory = injector.getInstance(ReportFactory.class);
        ProcessStage stage = injector.getInstance(ProcessStage.class);
        ReportInfo report = factory.create(sources);
        stage.run(report, output);
        Set<String> pluginsWithStatic = unpackPluginStatic(plugins, output);
        writeIndexHtml(pluginsWithStatic, output);
    }

    private static Injector createInjector(List<Plugin> plugins) {
        List<Module> enabledPluginsModules = plugins.stream()
                .filter(Plugin::isEnabled)
                .map(Plugin::getModule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return Guice.createInjector(new ParentModule(plugins, enabledPluginsModules));
    }

    private static void writeIndexHtml(Set<String> pluginsWithStatic, Path outputDirectory) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassLoaderForTemplateLoading(Main.class.getClassLoader(), "tpl");
        Path indexHtml = outputDirectory.resolve("index.html");
        try (BufferedWriter writer = Files.newBufferedWriter(indexHtml, StandardOpenOption.CREATE)) {
            Template template = cfg.getTemplate("index.html.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("plugins", pluginsWithStatic);
            template.process(dataModel, writer);
        } catch (IOException | TemplateException e) {
            LOGGER.error("Could't read index file", e);
        }
    }

    private static Set<String> unpackPluginStatic(List<Plugin> plugins, Path outputDirectory) {
        Path pluginsDirectory = outputDirectory.resolve("plugins");
        return plugins.stream()
                .filter(Plugin::isEnabled)
                .map(plugin -> copyPluginStatic(plugin, pluginsDirectory))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<String> copyPluginStatic(Plugin plugin, Path pluginsDirectory) {
        String name = plugin.getDescriptor().getName();
        Path pluginDirectory = pluginsDirectory.resolve(name);
        try (ZipFile zipFile = new ZipFile(plugin.getArchive().toFile())) {
            boolean anyCopied = zipFile.stream()
                    .filter(entry -> entry.getName().startsWith(STATIC_FILES_PREFIX))
                    .map(entry -> unpackEntry(zipFile, entry, pluginDirectory))
                    .anyMatch(Boolean::booleanValue);
            if (anyCopied) {
                return Optional.of(name);
            }
        } catch (IOException e) {
            LOGGER.error("Could not read <{}> plugin archive {}", name, e);
        }
        return Optional.empty();
    }

    private static boolean unpackEntry(ZipFile zipFile, ZipEntry entry, Path pluginDirectory) {
        try (InputStream is = zipFile.getInputStream(entry)) {
            Files.createDirectories(pluginDirectory);
            String entryPath = entry.getName().substring(STATIC_FILES_PREFIX.length());
            Files.copy(is, pluginDirectory.resolve(entryPath));
            return true;
        } catch (IOException e) {
            LOGGER.error("Could not copy plugin entry {} {}", entry, e);
            return false;
        }
    }
}
