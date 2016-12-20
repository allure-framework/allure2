package org.allurefw.report.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author charlie (Dmitry Baev).
 */
public class CopyVisitor extends SimpleFileVisitor<Path> {

    private final Path sourceDirectory;

    private final Path outputDirectory;

    public CopyVisitor(Path sourceDirectory, Path outputDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Files.createDirectories(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path dest = outputDirectory.resolve(sourceDirectory.relativize(file));
        Files.copy(file, dest);
        return FileVisitResult.CONTINUE;
    }
}
