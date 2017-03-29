package io.qameta.allure.core;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.Plugin;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.freemarker.FreemarkerContext;
import io.qameta.allure.utils.CopyVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class IndexHtmlPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexHtmlPlugin.class);

    @Override
    public void aggregate(final ReportConfiguration configuration,
                          final List<LaunchResults> launches,
                          final Path outputDirectory) throws IOException {
        final Path pluginsFolder = Files.createDirectories(outputDirectory.resolve("plugins"));
        final Set<String> pluginNames = configuration.getPlugins().stream()
                .filter(Plugin::isEnabled)
                .map(plugin -> unpackStatic(plugin, pluginsFolder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        final FreemarkerContext context = configuration.requireContext(FreemarkerContext.class);
        final Path indexHtml = outputDirectory.resolve("index.html");
        try (BufferedWriter writer = Files.newBufferedWriter(indexHtml, StandardOpenOption.CREATE)) {
            final Template template = context.getValue().getTemplate("index.html.ftl");
            final Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("plugins", pluginNames);
            template.process(dataModel, writer);
        } catch (TemplateException e) {
            LOGGER.error("Could't write index file", e);
        }
    }

    private static Optional<String> unpackStatic(final Plugin plugin, final Path outputDirectory) {
        final String name = plugin.getDescriptor().getName();
        final Path pluginOutputDirectory = outputDirectory.resolve(name);
        unpack(plugin, pluginOutputDirectory);
        return Files.exists(pluginOutputDirectory.resolve("index.js"))
                ? Optional.of(name)
                : Optional.empty();
    }

    private static void unpack(final Plugin plugin, final Path outputDirectory) {
        final Path pluginStatic = plugin.getPluginDirectory().resolve("static");
        if (Files.notExists(pluginStatic)) {
            return;
        }
        try {
            Files.walkFileTree(pluginStatic, new CopyVisitor(pluginStatic, outputDirectory));
        } catch (IOException e) {
            LOGGER.error("Could not copy plugin static {} {}", plugin.getDescriptor().getName(), e);
        }
    }
}
