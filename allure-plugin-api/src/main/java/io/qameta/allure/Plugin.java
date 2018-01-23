package io.qameta.allure;

import io.qameta.allure.config.PluginConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Base plugin interface.
 *
 * @since 2.0
 */
public interface Plugin {

    PluginConfiguration getConfig();

    void unpackReportStatic(Path outputDirectory) throws IOException;

    List<Extension> getExtensions();

}
