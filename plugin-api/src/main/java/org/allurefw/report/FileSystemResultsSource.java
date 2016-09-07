package org.allurefw.report;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class FileSystemResultsSource implements ResultsSource {

    public static final Logger LOGGER = LoggerFactory.getLogger(FileSystemResultsSource.class);

    private final Path resultsDirectory;

    @Inject
    public FileSystemResultsSource(@Assisted Path resultsDirectory) {
        this.resultsDirectory = resultsDirectory;
    }

    @Override
    public List<String> getResultsByGlob(String glob) {
        return ReportApiUtils.listFiles(resultsDirectory, glob).stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream getResult(String name) {
        try {
            return Files.newInputStream(resultsDirectory.resolve(name));
        } catch (IOException e) {
            LOGGER.error("Could not get content of result {} {}", name, e);
            //TODO think about better solution
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getSize(String name) {
        try {
            return Files.size(resultsDirectory.resolve(name));
        } catch (IOException e) {
            LOGGER.debug("Could not get size of result {} {}", name, e);
            return 0;
        }
    }
}
