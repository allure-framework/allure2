package org.allurefw.report;

import org.allurefw.report.entity.Attachment;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.size;
import static org.apache.tika.mime.MimeTypes.getDefaultMimeTypes;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 30.01.16
 */
public final class ReportApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportApiUtils.class);

    private static final Metadata METADATA = new Metadata();

    public static final Integer RADIX = 16;

    public static final int UID_RANDOM_BYTES_COUNT = 8;

    ReportApiUtils() {
    }

    public static String generateUid() {
        SecureRandom rand = new SecureRandom();
        byte[] randomBytes = new byte[UID_RANDOM_BYTES_COUNT];
        rand.nextBytes(randomBytes);
        return new BigInteger(1, randomBytes).toString(RADIX);
    }

    public static Properties loadProperties(Properties properties, String fileName, Path... directories) {
        for (Path path : directories) {
            Path env = path.resolve(fileName);
            if (notExists(env)) {
                continue;
            }
            try (InputStream stream = newInputStream(env)) {
                properties.load(stream);
            } catch (IOException e) {
                LOGGER.debug("Could not read properties from file " + path, e);
            }
        }

        return properties;
    }

    public static String getExtensionByMimeType(String type) {
        try {
            return getDefaultMimeTypes().forName(type).getExtension();
        } catch (Exception e) {
            LOGGER.warn("Can't detect extension for MIME-type {} {}", type, e);
            return "";
        }
    }

    public static String probeContentType(Path path) {
        try (InputStream stream = newInputStream(path)) {
            return probeContentType(stream, path.getFileName().toString());
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the mime-type of attachment {} {}", path, e);
            return "unknown";
        }
    }

    public static String probeContentType(InputStream is, String name) {
        try (InputStream stream = new BufferedInputStream(is)) {
            return getDefaultMimeTypes().detect(stream, METADATA).toString();
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the mime-type of attachment {} {}", name, e);
            return "unknown";
        }
    }

    public static List<Path> listFiles(Path directory, String glob) {
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, glob)) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Could not list files by glob {} in directory {}: {}", glob, directory, e);
            return Collections.emptyList();
        }
    }

    public static Attachment createAttachment(Path attachmentFile) {
        return createAttachment(attachmentFile, null);
    }

    public static Attachment createAttachment(Path attachmentFile, String mimeType) {
        String uid = generateUid();
        String fileName = attachmentFile.getFileName().toString();
        String realType = Optional.ofNullable(mimeType)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> ReportApiUtils.probeContentType(attachmentFile));
        String extension = Optional.of(getFileExtension(fileName))
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> ReportApiUtils.getExtensionByMimeType(realType));
        String fileNameWithoutExtension = getNameWithoutExtension(fileName);
        String source = uid + (extension.isEmpty() ? "" : "." + extension);
        Long size = getFileSizeSafe(attachmentFile);

        return new Attachment()
                .withUid(uid)
                .withName(fileNameWithoutExtension)
                .withSource(source)
                .withType(realType)
                .withSize(size);
    }

    private static Long getFileSizeSafe(Path path) {
        try {
            return size(path);
        } catch (IOException e) {
            LOGGER.warn("Could not get the size of file {} {}", path, e);
            return null;
        }
    }
}
