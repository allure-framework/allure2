package io.qameta.allure.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Visitor that recursive copies directories.
 *
 * @since 2.0
 */
public class CopyVisitor extends SimpleFileVisitor<Path> {

    private final Path sourceDirectory;

    private final Path outputDirectory;

    public CopyVisitor(final Path sourceDirectory, final Path outputDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        Files.createDirectories(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        final Path dest = outputDirectory.resolve(sourceDirectory.relativize(file));
        Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }
}
