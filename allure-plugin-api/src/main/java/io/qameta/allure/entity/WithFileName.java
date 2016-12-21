package io.qameta.allure.entity;

import com.google.common.io.Files;

import java.nio.file.Paths;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 05.02.16
 */
public interface WithFileName {

    String getPath();

    default String getFileName() {
        return Paths.get(getPath()).getFileName().toString();
    }

    default String getFileExtension() {
        return Files.getFileExtension(getPath());
    }
}
