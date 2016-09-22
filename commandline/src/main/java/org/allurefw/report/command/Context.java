package org.allurefw.report.command;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class Context {

    private final Path workDirectory;

    private final Path pluginsDirectory;

    private final Path webDirectory;

    private final String toolVersion;

    public Context(Path workDirectory, Path pluginsDirectory, Path webDirectory, String toolVersion) {
        this.workDirectory = workDirectory;
        this.pluginsDirectory = pluginsDirectory;
        this.webDirectory = webDirectory;
        this.toolVersion = toolVersion;
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
}
