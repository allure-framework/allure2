package org.allurefw.report.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.allurefw.report.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class DefaultWriter implements Writer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWriter.class);

    private final ObjectMapper mapper;

    @Inject
    public DefaultWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void write(Path outputDirectory, String fileName, Object object) {
        Path dest = outputDirectory.resolve(fileName);
        try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(dest))) {
            mapper.writeValue(stream, object);
        } catch (IOException e) {
            LOGGER.warn("Couldn't write {} to {}: {}", object.getClass(), dest, e);
        }
    }

    @Override
    public void write(Path outputDirectory, String fileName, Path source) {
        Path dest = outputDirectory.resolve(fileName);
        try {
            Files.createDirectories(outputDirectory);
            Files.copy(source, dest);
        } catch (IOException e) {
            LOGGER.error("Couldn't copy file {} to {}: {}", source, dest, e);
        }
    }
}
