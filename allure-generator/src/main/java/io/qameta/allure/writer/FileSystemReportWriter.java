package io.qameta.allure.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.ReportGenerationException;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.TestCaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * @author charlie (Dmitry Baev).
 */
public class FileSystemReportWriter implements ReportWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemReportWriter.class);

    private final Path dataDirectory;

    private final Path attachmentDirectory;

    private final Path testCasesDirectory;

    private ObjectMapper mapper;

    public FileSystemReportWriter(final Path reportDirectory) {
        this(new ObjectMapper(), reportDirectory);
    }

    public FileSystemReportWriter(final ObjectMapper mapper, final Path reportDirectory) {
        this.mapper = mapper;
        this.dataDirectory = createDirectories(reportDirectory.resolve("data"));
        this.attachmentDirectory = createDirectories(dataDirectory.resolve("attachments"));
        this.testCasesDirectory = createDirectories(dataDirectory.resolve("test-cases"));
    }

    @Override
    public void writeTestCase(final TestCaseResult result) {
        final Path dest = testCasesDirectory.resolve(result.getSource());
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(dest, CREATE_NEW))) {
            mapper.writeValue(os, result);
        } catch (IOException e) {
            LOGGER.error("Couldn't write test case {} to {}: {}", result.getFullName(), dest, e);
        }
    }

    @Override
    public void writeAttachment(final InputStream attachmentBody, final Attachment attachment) {
        final Path dest = attachmentDirectory.resolve(attachment.getSource());
        try {
            Files.copy(attachmentBody, dest);
        } catch (IOException e) {
            LOGGER.error("Couldn't write attachment {} to {}: {}", attachment.getName(), dest, e);
        }
    }

    @Override
    public void writeJsonData(final String fileName, final Object data) {
        final Path dest = dataDirectory.resolve(fileName);
        try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(dest))) {
            mapper.writeValue(stream, data);
        } catch (IOException e) {
            LOGGER.error("Couldn't write data {} to {}: {}", fileName, dest, e);
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(final ObjectMapper mapper) {
        Objects.requireNonNull(mapper, "ObjectMapper should not be a null");
        this.mapper = mapper;
    }

    private Path createDirectories(final Path outputDirectory) {
        try {
            return Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw new ReportGenerationException("Could not create report directory " + outputDirectory, e);
        }
    }
}
