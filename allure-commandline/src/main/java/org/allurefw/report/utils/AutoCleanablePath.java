package org.allurefw.report.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class AutoCleanablePath implements AutoCloseable {

    private final Path path;

    public AutoCleanablePath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void close() throws Exception {
        Files.walkFileTree(path, new DeleteVisitor());
    }

    public static AutoCleanablePath create(String prefix) throws IOException {
        return new AutoCleanablePath(Files.createTempDirectory(prefix));
    }
}
