package org.allurefw.report;

import com.google.common.reflect.ClassPath;
import com.google.inject.Guice;
import com.google.inject.Module;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.allurefw.report.defects.DefectsPlugin;
import org.allurefw.report.graph.GraphPlugin;
import org.allurefw.report.jackson.JacksonMapperModule;
import org.allurefw.report.timeline.TimelinePlugin;
import org.allurefw.report.total.TotalPlugin;
import org.allurefw.report.writer.WriterModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.copy;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 30.01.16
 */
public class ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);

    private final Path[] inputs;

    public ReportGenerator(Path... inputs) {
        this.inputs = inputs;
    }

    public void generate(Path output) {
        createDirectory(output, "Could not create output directory");

        List<AbstractPlugin> plugins = getPlugins();
        Set<String> pluginsWithStaticContent = Collections.emptySet();
        writeIndexHtml(output, pluginsWithStaticContent);

        Path pluginsDir = output.resolve("plugins");
        pluginsWithStaticContent.forEach(pluginName -> {
            Path pluginDir = pluginsDir.resolve(pluginName);
            unpackReportPlugin(pluginDir, pluginName);
        });
        Guice.createInjector(new ProcessStageModule())
                .createChildInjector(getModules())
                .createChildInjector(plugins)
                .getInstance(ProcessStage.class)
                .run(null, output);
    }

    private List<AbstractPlugin> getPlugins() {
        return Arrays.asList(
                new DefectsPlugin(),
                new TimelinePlugin(),
                new GraphPlugin()
        );
    }

    private List<Module> getModules() {
        return Arrays.asList(
                new JacksonMapperModule(),
                new WriterModule(),
                new TotalPlugin()
        );
    }

    private void unpackReportPlugin(Path outputDirectory, String pluginName) {
        try {
            Pattern pattern = Pattern.compile("^allure" + pluginName + "/(.+)");
            ClassLoader loader = getClass().getClassLoader();
            for (ClassPath.ResourceInfo info : ClassPath.from(loader).getResources()) {
                Matcher matcher = pattern.matcher(info.getResourceName());
                if (matcher.find()) {
                    String resourcePath = matcher.group(1);
                    Path dest = outputDirectory.resolve(resourcePath);
                    try (InputStream input = info.url().openStream()) {
                        Files.createDirectories(dest.getParent());
                        copy(input, dest);
                        LOGGER.debug("{} successfully copied.", resourcePath);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error during plugin unpack: {}", e);
        }
    }

    public void writeIndexHtml(Path outputDirectory, Set<String> facePluginNames) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "tpl");
        Path indexHtml = outputDirectory.resolve("index.html");
        try (BufferedWriter writer = Files.newBufferedWriter(indexHtml, StandardOpenOption.CREATE)) {
            Template template = cfg.getTemplate("index.html.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("plugins", facePluginNames);
            template.process(dataModel, writer);
        } catch (IOException | TemplateException e) {
            LOGGER.error("Could't read index file", e);
        }
    }

    public void createDirectory(Path directory, String message) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(message, e);
        }
    }
}
