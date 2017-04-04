package io.qameta.allure.core;

import io.qameta.allure.PluginConfiguration;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class PluginDescriptor {

    private final PluginConfiguration configuration;

    private final Path pluginDirectory;

    public PluginDescriptor(final PluginConfiguration configuration, final Path pluginDirectory) {
        this.configuration = configuration;
        this.pluginDirectory = pluginDirectory;
    }

    public PluginConfiguration getConfiguration() {
        return configuration;
    }

    public Path getPluginDirectory() {
        return pluginDirectory;
    }
}
