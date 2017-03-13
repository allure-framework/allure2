package io.qameta.allure.command;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class Context {

    private final Path workDirectory;

    private final Path pluginsDirectory;

    private final Path webDirectory;

    private final String toolVersion;

    private final Set<String> enabledPlugins;

    public Context(final Path workDirectory, final Path pluginsDirectory, final Path webDirectory,
                   final String toolVersion, final Set<String> enabledPlugins) {
        this.workDirectory = workDirectory;
        this.pluginsDirectory = pluginsDirectory;
        this.webDirectory = webDirectory;
        this.toolVersion = toolVersion;
        this.enabledPlugins = enabledPlugins;
    }

    public Path getWorkDirectory() {
        return workDirectory;
    }

    public Path getPluginsDirectory() {
        return pluginsDirectory;
    }

    public Path getWebDirectory() {
        return webDirectory;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public Set<String> getEnabledPlugins() {
        return enabledPlugins;
    }
}
