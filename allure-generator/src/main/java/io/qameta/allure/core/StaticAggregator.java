package io.qameta.allure.core;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.utils.CopyVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class StaticAggregator implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticAggregator.class);

    private final List<String> staticFiles = Arrays.asList("app.js", "styles.css");

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final Set<String> pluginNames = writePluginsStatic(configuration, outputDirectory);
        writeIndexHtml(configuration, outputDirectory, pluginNames);
        writeStatic(outputDirectory);
    }

    private Set<String> writePluginsStatic(final Configuration configuration,
                                           final Path outputDirectory) throws IOException {
        final Path pluginsFolder = Files.createDirectories(outputDirectory.resolve("plugins"));
        return configuration.getPluginsDescriptors().stream()
                .map(plugin -> unpackStatic(plugin, pluginsFolder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private void writeIndexHtml(final Configuration configuration,
                                final Path outputDirectory,
                                final Set<String> pluginNames) throws IOException {
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

    private void writeStatic(final Path outputDirectory) {
        staticFiles.forEach(resourceName -> {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                Files.copy(is, outputDirectory.resolve(resourceName));
            } catch (IOException e) {
                LOGGER.error("Couldn't unpack report static");
            }
        });
    }

    private static Optional<String> unpackStatic(final PluginDescriptor pluginDescriptor, final Path outputDirectory) {
        final String name = pluginDescriptor.getConfiguration().getName();
        final Path pluginOutputDirectory = outputDirectory.resolve(name);
        unpack(pluginDescriptor, pluginOutputDirectory);
        return Files.exists(pluginOutputDirectory.resolve("index.js"))
                ? Optional.of(name)
                : Optional.empty();
    }

    private static void unpack(final PluginDescriptor pluginDescriptor, final Path outputDirectory) {
        final Path pluginStatic = pluginDescriptor.getPluginDirectory().resolve("static");
        if (Files.notExists(pluginStatic)) {
            return;
        }
        try {
            Files.walkFileTree(pluginStatic, new CopyVisitor(pluginStatic, outputDirectory));
        } catch (IOException e) {
            LOGGER.error(
                    "Could not copy pluginDescriptor static {} {}",
                    pluginDescriptor.getConfiguration().getName(),
                    e
            );
        }
    }
}
