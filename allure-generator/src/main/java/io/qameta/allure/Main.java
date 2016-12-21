package io.qameta.allure;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.plugins.DefaultPluginLoader;
import io.qameta.allure.plugins.EmptyPluginsLoader;
import io.qameta.allure.utils.CopyVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
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

/**
 * @author charlie (Dmitry Baev).
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final PluginsLoader pluginsLoader;

    private final Set<String> enabledPlugins;

    public Main() {
        this(new EmptyPluginsLoader(), Collections.emptySet());
    }

    public Main(Path pluginsDirectory, Set<String> enabledPlugins) {
        this(new DefaultPluginLoader(pluginsDirectory), enabledPlugins);
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
        LOGGER.debug("Found {} plugins", plugins.size());
        plugins.forEach(plugin ->
                LOGGER.debug("<{}>, enabled: {}", plugin.getDescriptor().getName(), plugin.isEnabled())
        );
        Injector injector = createInjector(plugins);
        ProcessStage stage = injector.getInstance(ProcessStage.class);
        Statistic run = stage.run(output, sources);
        LOGGER.debug("## Summary");
        LOGGER.debug("Found {} test cases ({} failed, {} broken)", run.getTotal(), run.getFailed(), run.getBroken());
        LOGGER.debug("Success percentage: {}", getSuccessPercentage(run));
        LOGGER.debug("Creating index.html...");
        Set<String> pluginsWithStatic = unpackStatic(plugins, output);
        writeIndexHtml(pluginsWithStatic, output);
    }

    private String getSuccessPercentage(Statistic run) {
        return run.getTotal() == 0 ? "Unknown" : String.valueOf(run.getPassed() * 100 / run.getTotal());
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
        cfg.setLocalizedLookup(false);
        cfg.setTemplateUpdateDelayMilliseconds(0);
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

    private static Set<String> unpackStatic(List<Plugin> plugins, Path outputDirectory) {
        Path pluginsDirectory = outputDirectory.resolve("plugins");
        return plugins.stream()
                .filter(Plugin::isEnabled)
                .map(plugin -> unpackStatic(plugin, pluginsDirectory))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public static Optional<String> unpackStatic(Plugin plugin, Path outputDirectory) {
        String name = plugin.getDescriptor().getName();
        Path pluginOutputDirectory = outputDirectory.resolve(name);
        unpack(plugin, pluginOutputDirectory);
        return Files.exists(pluginOutputDirectory.resolve("index.js"))
                ? Optional.of(name)
                : Optional.empty();
    }

    private static void unpack(Plugin plugin, Path outputDirectory) {
        Path pluginStatic = plugin.getPluginDirectory().resolve("static");
        if (Files.notExists(pluginStatic)) {
            return;
        }
        try {
            Files.createDirectories(outputDirectory);
            Files.walkFileTree(pluginStatic, new CopyVisitor(pluginStatic, outputDirectory));
        } catch (IOException e) {
            LOGGER.error("Could not copy plugin static {} {}", plugin.getDescriptor().getName(), e);
        }
    }
}
