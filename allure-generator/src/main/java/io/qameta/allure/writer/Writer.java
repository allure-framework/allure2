package io.qameta.allure.writer;

import java.nio.file.Path;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 17.02.16
 */
public interface Writer {

    void write(Path outputDirectory, String fileName, Object object);

    void write(Path outputDirectory, String fileName, Path source);
}
