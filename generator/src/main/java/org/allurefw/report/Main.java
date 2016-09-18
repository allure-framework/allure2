package org.allurefw.report;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.allurefw.report.plugins.DefaultPluginsLoader;
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

    private final Path pluginsDirectory;

    private final Path workDirectory;

    private final Set<String> enabledPlugins;

    public Main(Path pluginsDirectory, Path workDirectory, Set<String> enabledPlugins) {
        this.pluginsDirectory = pluginsDirectory;
        this.workDirectory = workDirectory;
        this.enabledPlugins = enabledPlugins;
    }

    public List<Plugin> loadPlugins() {
        PluginsLoader pluginsLoader = new DefaultPluginsLoader(
                pluginsDirectory,
                workDirectory
        );
        return pluginsLoader.loadPlugins();
    }

    public boolean isPluginEnabled(Plugin plugin) {
        return enabledPlugins.contains(plugin.getDescriptor().getName());
    }

    public List<Plugin> getEnabledPlugins() {
        return loadPlugins().stream()
                .filter(this::isPluginEnabled)
                .collect(Collectors.toList());
    }

    public List<Module> getEnabledPluginsModules() {
        return getEnabledPlugins().stream()
                .map(Plugin::getModule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Report createReport(Path... sources) {
        ReportFactory factory = createInjector()
                .getInstance(ReportFactory.class);
        return factory.create(sources);
    }

    public void generate(Path output, Path... sources) {
        Injector injector = createInjector();
        ReportFactory factory = injector.getInstance(ReportFactory.class);
        ProcessStage stage = injector.getInstance(ProcessStage.class);
        Report report = factory.create(sources);
        stage.run(report, output);
        writeIndexHtml(output);
    }

    private void writeIndexHtml(Path outputDirectory) {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "tpl");
        Path indexHtml = outputDirectory.resolve("index.html");
        try (BufferedWriter writer = Files.newBufferedWriter(indexHtml, StandardOpenOption.CREATE)) {
            Template template = cfg.getTemplate("index.html.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("plugins", Collections.emptySet());
            template.process(dataModel, writer);
        } catch (IOException | TemplateException e) {
            LOGGER.error("Could't read index file", e);
        }
    }

    private Injector createInjector() {
        return Guice.createInjector(new ParentModule(loadPlugins()))
                .createChildInjector(getEnabledPluginsModules());
    }

    public static void main(String[] args) {
    }
}
