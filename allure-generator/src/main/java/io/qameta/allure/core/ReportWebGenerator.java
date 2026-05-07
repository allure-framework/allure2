/*
 *  Copyright 2016-2026 Qameta Software Inc
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportWebGenerator {

    private static final String VITE_MANIFEST_JSON = "vite-manifest.json";

    private static final String IMAGE_X_ICON = "image/x-icon";
    private static final String TEXT_JAVASCRIPT = "text/javascript; charset=utf-8";
    private static final String TEXT_CSS = "text/css; charset=utf-8";

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final TypeReference<Map<String, ViteManifestEntry>> VITE_MANIFEST_TYPE = new TypeReference<Map<String, ViteManifestEntry>>() {
    };

    public void generate(final Configuration configuration,
                         final ReportStorage reportStorage,
                         final Path outputDirectory) {

        final boolean inline = reportStorage instanceof InMemoryReportStorage;

        final Map<String, ViteManifestEntry> viteManifest = loadViteManifest();
        final List<String> entryFiles = getEntryFiles(viteManifest);
        final List<String> styleFiles = getStyleFiles(viteManifest);
        final List<String> producedFiles = getProducedFiles(viteManifest);
        final String faviconFile = requireFaviconFile(viteManifest);

        final List<String> coreStyleUrls = inline
                ? inlineWebStyles(styleFiles)
                : new ArrayList<>(styleFiles);
        if (!inline) {
            copyWebResources(outputDirectory, producedFiles);
        }
        final List<String> coreJsUrls = inline
                ? inlineWebScripts(entryFiles)
                : new ArrayList<>(entryFiles);

        final List<String> pluginJsUrls = new ArrayList<>();
        final List<String> pluginStyleUrls = new ArrayList<>();
        configuration.getPlugins().forEach(plugin -> {
            final Map<String, Path> pluginFiles = new HashMap<>(plugin.getPluginFiles());

            final PluginConfiguration config = plugin.getConfig();
            config.getJsFiles().forEach(jsFile -> {
                final Path jsFilePath = pluginFiles.remove(jsFile);
                if (inline) {
                    pluginJsUrls.add(
                            dataBase64(
                                    TEXT_JAVASCRIPT,
                                    jsFilePath
                            )
                    );
                } else {
                    final String key = pluginFileKey(config, jsFile);
                    pluginJsUrls.add(key);
                    write(outputDirectory, key, jsFilePath);
                }
            });

            config.getCssFiles().forEach(cssFile -> {
                final Path cssFilePath = pluginFiles.remove(cssFile);
                if (inline) {
                    pluginStyleUrls.add(
                            dataBase64(
                                    TEXT_CSS,
                                    cssFilePath
                            )
                    );
                } else {
                    final String key = pluginFileKey(config, cssFile);
                    pluginStyleUrls.add(key);
                    write(outputDirectory, key, cssFilePath);
                }
            });

            pluginFiles.forEach((key, path) -> {
                final String pluginFileKey = pluginFileKey(config, key);
                write(
                        outputDirectory,
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

            final String faviconUrl = inline ? dataBase64(IMAGE_X_ICON, faviconFile) : faviconFile;

            dataModel.put("faviconUrl", faviconUrl);
            dataModel.put("coreStyleUrls", coreStyleUrls);
            dataModel.put("pluginStyleUrls", pluginStyleUrls);
            dataModel.put("coreJsUrls", coreJsUrls);
            dataModel.put("pluginJsUrls", pluginJsUrls);

            if (inline) {
                final Map<String, String> reportDataFiles = new HashMap<>(
                        ((InMemoryReportStorage) reportStorage)
                                .getReportDataFiles()
                );

                dataModel.put("reportDataFiles", reportDataFiles);
            }

            final boolean analyticsDisable = Optional.ofNullable(System.getenv(Constants.NO_ANALYTICS))
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            dataModel.put("analyticsDisable", analyticsDisable);

            dataModel.put("reportUuid", configuration.getUuid());
            dataModel.put("reportName", configuration.getReportName());
            dataModel.put("reportLanguage", configuration.getReportLanguage());
            dataModel.put("allureVersion", configuration.getVersion());

            template.process(dataModel, writer);
        } catch (Exception e) {
            throw new ReportGenerationException("could not generate report web", e);
        }
    }

    private static Map<String, ViteManifestEntry> loadViteManifest() {
        try {
            return JSON_MAPPER.readValue(readResource(VITE_MANIFEST_JSON), VITE_MANIFEST_TYPE);
        } catch (IOException e) {
            throw new ReportGenerationException("Can't read Vite manifest", e);
        }
    }

    private static void copyWebResources(final Path outputDirectory,
                                         final List<String> producedFiles) {
        producedFiles.forEach(file -> write(outputDirectory, file, readResource(file)));
    }

    private static List<String> getEntryFiles(final Map<String, ViteManifestEntry> viteManifest) {
        final List<String> entryFiles = new ArrayList<>();

        viteManifest.forEach((key, entry) -> {
            if (entry.isEntry()) {
                entryFiles.add(requireManifestFile(entry, key));
            }
        });

        if (entryFiles.isEmpty()) {
            throw new ReportGenerationException("Vite manifest does not contain entry chunks");
        }

        return entryFiles;
    }

    private static List<String> getStyleFiles(final Map<String, ViteManifestEntry> viteManifest) {
        final Set<String> styleFiles = new LinkedHashSet<>();

        getReachableManifestKeys(viteManifest).forEach(
                key -> styleFiles.addAll(requireManifestEntry(viteManifest, key).getCss())
        );

        return new ArrayList<>(styleFiles);
    }

    private static List<String> getProducedFiles(final Map<String, ViteManifestEntry> viteManifest) {
        final Set<String> producedFiles = new LinkedHashSet<>();

        viteManifest.values().forEach(entry -> {
            addIfPresent(producedFiles, entry.getFile());
            producedFiles.addAll(entry.getCss());
            producedFiles.addAll(entry.getAssets());
        });

        return new ArrayList<>(producedFiles);
    }

    private static List<String> inlineWebStyles(final List<String> styleFiles) {
        final List<String> inlineStyles = new ArrayList<>();

        styleFiles.forEach(styleFile -> inlineStyles.add(dataBase64(TEXT_CSS, styleFile)));

        return inlineStyles;
    }

    private static List<String> inlineWebScripts(final List<String> entryFiles) {
        final List<String> scriptUrls = new ArrayList<>();

        entryFiles.forEach(entryFile -> scriptUrls.add(dataBase64(TEXT_JAVASCRIPT, entryFile)));

        return scriptUrls;
    }

    private static String pluginFileKey(final PluginConfiguration config, final String cssFile) {
        return "plugin/" + config.getId() + "/" + cssFile;
    }

    private static List<String> getReachableManifestKeys(final Map<String, ViteManifestEntry> viteManifest) {
        final List<String> queue = new ArrayList<>();
        final Set<String> visited = new LinkedHashSet<>();

        viteManifest.forEach((key, entry) -> {
            if (entry.isEntry()) {
                queue.add(key);
            }
        });

        while (!queue.isEmpty()) {
            final String key = queue.remove(0);
            if (!visited.add(key)) {
                continue;
            }

            final ViteManifestEntry entry = requireManifestEntry(viteManifest, key);
            queue.addAll(entry.getImports());
            queue.addAll(entry.getDynamicImports());
        }

        return new ArrayList<>(visited);
    }

    private static String requireFaviconFile(final Map<String, ViteManifestEntry> viteManifest) {
        return viteManifest.values().stream()
                .map(ViteManifestEntry::getFile)
                .filter(Objects::nonNull)
                .filter(file -> file.endsWith(".ico"))
                .findFirst()
                .orElseThrow(() -> new ReportGenerationException("Web favicon is not present in the Vite manifest"));
    }

    private static ViteManifestEntry requireManifestEntry(final Map<String, ViteManifestEntry> viteManifest,
                                                          final String key) {
        return Optional.ofNullable(viteManifest.get(key))
                .orElseThrow(
                        () -> new ReportGenerationException(
                                String.format("Vite manifest entry %s is not present", key)
                        )
                );
    }

    private static String requireManifestFile(final ViteManifestEntry entry,
                                              final String key) {
        return Optional.ofNullable(entry.getFile())
                .filter(file -> !file.isEmpty())
                .orElseThrow(
                        () -> new ReportGenerationException(
                                String.format("Vite manifest entry %s does not define a file", key)
                        )
                );
    }

    private static void addIfPresent(final Set<String> values, final String value) {
        if (value != null && !value.isEmpty()) {
            values.add(value);
        }
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    private static byte[] readResource(final String resourceName) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resourceName)) {
            if (Objects.isNull(is)) {
                throw new ReportGenerationException(
                        String.format("Resource %s not found", resourceName)
                );
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
