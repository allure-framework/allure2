package io.qameta.allure.plugin;

import io.qameta.allure.Extension;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.util.CopyVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

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
        final Path pluginStatic = pluginDirectory.resolve("static");
        if (Files.exists(pluginStatic)) {
            Files.walkFileTree(pluginStatic, new CopyVisitor(pluginStatic, outputDirectory));
        }
    }

    @Override
    public List<Extension> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }
}
