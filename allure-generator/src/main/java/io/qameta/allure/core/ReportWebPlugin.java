/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.core;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.qameta.allure.Aggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.ReportGenerationException;
import io.qameta.allure.context.FreemarkerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Plugins that stores report static files to data directory.
 *
 * @since 2.0
 */
public class ReportWebPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportWebPlugin.class);

    private final List<String> staticFiles = Arrays.asList("app.js", "styles.css", "favicon.ico");

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        writePluginsStatic(configuration, outputDirectory);
        writeIndexHtml(configuration, outputDirectory);
        writeStatic(outputDirectory);
    }

    protected void writePluginsStatic(final Configuration configuration,
                                      final Path outputDirectory) throws IOException {
        final Path pluginsFolder = outputDirectory.resolve(Constants.PLUGINS_DIR);
        for (Plugin plugin : configuration.getPlugins()) {
            final Path pluginDirectory = Files.createDirectories(pluginsFolder.resolve(plugin.getConfig().getId()));
            plugin.unpackReportStatic(pluginDirectory);
        }
    }

    protected void writeIndexHtml(final Configuration configuration,
                                  final Path outputDirectory) throws IOException {
        final FreemarkerContext context = configuration.requireContext(FreemarkerContext.class);
        final Path indexHtml = outputDirectory.resolve("index.html");
        final List<PluginConfiguration> pluginConfigurations = configuration.getPlugins().stream()
                .map(Plugin::getConfig)
                .collect(Collectors.toList());

        try (BufferedWriter writer = Files.newBufferedWriter(indexHtml)) {
            final Template template = context.getValue().getTemplate("index.html.ftl");
            final Map<String, Object> dataModel = new HashMap<>();
            dataModel.put(Constants.PLUGINS_DIR, pluginConfigurations);
            template.process(dataModel, writer);
        } catch (TemplateException e) {
            LOGGER.error("Could't write index file", e);
        }
    }

    protected void writeStatic(final Path outputDirectory) {
        staticFiles.forEach(resourceName -> {
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
                if (Objects.isNull(is)) {
                    throw new ReportGenerationException(String.format("Resource %s not found", resourceName));
                }
                Files.copy(is, outputDirectory.resolve(resourceName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Couldn't unpack report static");
            }
        });
    }
}
