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
import io.qameta.allure.Constants;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.ReportGenerationException;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.context.FreemarkerContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportWebGenerator {

    private static final String FAVICON_ICO = "favicon.ico";
    private static final String STYLES_CSS = "styles.css";
    private static final String APP_JS = "app.js";

    private static final String TEXT_JAVASCRIPT = "text/javascript";
    private static final String TEXT_CSS = "text/css";

    @SuppressWarnings({"ExecutableStatementCount", "PMD.NcssCount"})
    public void generate(final Configuration configuration,
                         final ReportStorage reportStorage,
                         final Path outputDirectory) {

        final boolean inline = reportStorage instanceof InMemoryReportStorage;

        final List<String> jsFiles = new ArrayList<>();
        if (inline) {
            jsFiles.add(dataBase64(TEXT_JAVASCRIPT, APP_JS));
        } else {
            jsFiles.add(APP_JS);
            write(outputDirectory, APP_JS, readResource(APP_JS));
        }

        final List<String> cssFiles = new ArrayList<>();
        if (inline) {
            cssFiles.add(dataBase64(TEXT_CSS, STYLES_CSS));
        } else {
            cssFiles.add(STYLES_CSS);
            write(outputDirectory, STYLES_CSS, readResource(STYLES_CSS));
        }

        configuration.getPlugins().forEach(plugin -> {
            final Map<String, Path> pluginFiles = new HashMap<>(plugin.getPluginFiles());

            final PluginConfiguration config = plugin.getConfig();
            config.getJsFiles().forEach(jsFile -> {
                final Path jsFilePath = pluginFiles.remove(jsFile);
                if (inline) {
                    jsFiles.add(dataBase64(
                            TEXT_JAVASCRIPT,
                            jsFilePath
                    ));
                } else {
                    final String key = pluginFileKey(config, jsFile);
                    jsFiles.add(key);
                    write(outputDirectory, key, jsFilePath);
                }
            });

            config.getCssFiles().forEach(cssFile -> {
                final Path cssFilePath = pluginFiles.remove(cssFile);
                if (inline) {
                    cssFiles.add(dataBase64(
                            TEXT_CSS,
                            cssFilePath
                    ));
                } else {
                    final String key = pluginFileKey(config, cssFile);
                    cssFiles.add(key);
                    write(outputDirectory, key, cssFilePath);
                }
            });

            pluginFiles.forEach((key, path) -> {
                final String pluginFileKey = pluginFileKey(config, key);
                write(outputDirectory,
                        pluginFileKey,
                        path
                );
            });
        });

        final FreemarkerContext context = configuration.requireContext(FreemarkerContext.class);

        try (Writer writer = Files
                .newBufferedWriter(
                        Files.createDirectories(outputDirectory).resolve("index.html"),
                        StandardCharsets.UTF_8
                )) {
            final Template template = context.getValue().getTemplate("index.html.ftl");
            final Map<String, Object> dataModel = new HashMap<>();


            final String faviconUrl = inline ? dataBase64("image/x-icon", FAVICON_ICO) : FAVICON_ICO;
            if (!inline) {
                write(outputDirectory, FAVICON_ICO, readResource(FAVICON_ICO));
            }

            dataModel.put("faviconUrl", faviconUrl);
            dataModel.put("stylesUrls", cssFiles);
            dataModel.put("jsUrls", jsFiles);

            if (inline) {
                final Map<String, String> reportDataFiles = new HashMap<>(((InMemoryReportStorage) reportStorage)
                        .getReportDataFiles());

                dataModel.put("reportDataFiles", reportDataFiles);
            }

            final boolean analyticsDisable = Optional.ofNullable(System.getenv(Constants.NO_ANALYTICS))
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            dataModel.put("analyticsDisable", analyticsDisable);
            dataModel.put("reportUuid", UUID.randomUUID().toString());
            dataModel.put("allureVersion", "dev");

            template.process(dataModel, writer);
        } catch (Exception e) {
            throw new ReportGenerationException("could not generate report web", e);
        }
    }

    private static String pluginFileKey(final PluginConfiguration config, final String cssFile) {
        return "plugin/" + config.getId() + "/" + cssFile;
    }

    private static byte[] readResource(final String resourceName) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resourceName)) {
            if (Objects.isNull(is)) {
                throw new ReportGenerationException(
                        String.format("Resource %s not found", resourceName));
            }
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new ReportGenerationException("Can't read resource " + resourceName, e);
        }
    }

    private static String dataBase64(final String contentType, final String resourceName) {
        final byte[] resource = readResource(resourceName);
        return dataUrl(contentType, resource);
    }

    private static String dataBase64(final String contentType, final Path path) {
        try {
            final byte[] bytes = FileUtils.readFileToByteArray(path.toFile());
            return dataUrl(contentType, bytes);
        } catch (IOException e) {
            throw new ReportGenerationException("Can't read file " + path, e);
        }
    }

    private static String dataUrl(final String contentType, final byte[] resource) {
        return String.format(
                "data:%s;base64,%s", contentType,
                Base64.getEncoder().encodeToString(resource)
        );
    }

    private static void write(final Path outputDirectory, final String name, final byte[] bytes) {
        try {
            final Path target = outputDirectory.resolve(name);
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new ReportGenerationException("Can't write bytes " + name, e);
        }
    }

    private static void write(final Path outputDirectory, final String name, final Path path) {
        try {
            final Path target = outputDirectory.resolve(name);
            Files.createDirectories(target.getParent());
            Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ReportGenerationException("Can't write file " + name, e);
        }
    }

}
