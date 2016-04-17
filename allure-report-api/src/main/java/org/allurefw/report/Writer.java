package org.allurefw.report;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public interface Writer {

    void write(Path outputDirectory, String fileName, Object object);

    void write(Path outputDirectory, String fileName, Path source);

    void writeIndexHtml(Path outputDirectory, Set<String> pluginNames);
}
