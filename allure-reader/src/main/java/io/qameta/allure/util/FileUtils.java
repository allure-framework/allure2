package io.qameta.allure.util;

import java.nio.file.Path;
import java.util.Optional;

/**
 * @author charlie (Dmitry Baev).
 */
public final class FileUtils {

    private FileUtils() {
        throw new IllegalStateException("Do not instance");
    }

    public static boolean endsWith(final Path file, final String suffix) {
        return Optional.ofNullable(file)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(fileName -> fileName.endsWith(suffix))
                .isPresent();
    }

    public static boolean matches(final Path file, final String regex) {
        return Optional.ofNullable(file)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(fileName -> fileName.matches(regex))
                .isPresent();
    }
}
