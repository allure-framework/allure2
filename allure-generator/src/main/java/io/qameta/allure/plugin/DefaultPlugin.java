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
package io.qameta.allure.plugin;

import io.qameta.allure.Extension;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.util.CopyVisitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default plugin implementation that unpack files from directory.
 *
 * @since 2.0
 */
public class DefaultPlugin implements Plugin {

    private final PluginConfiguration configuration;

    private final List<Extension> extensions;

    private final Path pluginDirectory;

    public DefaultPlugin(final PluginConfiguration configuration,
                         final List<Extension> extensions,
                         final Path pluginDirectory) {
        this.configuration = configuration;
        this.extensions = extensions;
        this.pluginDirectory = pluginDirectory;
    }

    @Override
    public PluginConfiguration getConfig() {
        return configuration;
    }

    @Override
    public void unpackReportStatic(final Path outputDirectory) throws IOException {
        final Path pluginStatic = getPluginStatic();
        if (Files.exists(pluginStatic)) {
            Files.walkFileTree(pluginStatic, new CopyVisitor(pluginStatic, outputDirectory));
        }
    }

    @Override
    public Map<String, Path> getPluginFiles() {
        final Path pluginStatic = getPluginStatic();
        final Map<String, Path> result = new HashMap<>();
        if (Files.exists(pluginStatic)) {
            try {
                final SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(final Path file,
                                                     final BasicFileAttributes attrs) {
                        final String fileId = pluginStatic.relativize(file).toString();
                        result.put(fileId, file);
                        return FileVisitResult.CONTINUE;
                    }
                };
                Files.walkFileTree(pluginStatic, visitor);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public List<Extension> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

    private Path getPluginStatic() {
        return pluginDirectory.resolve("static");
    }
}
