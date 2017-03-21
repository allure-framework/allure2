package io.qameta.allure;

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
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newInputStream;
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

    private ReportApiUtils() {
        throw new IllegalStateException("Do not instance");
    }

    public static String generateUid() {
        byte[] randomBytes = new byte[UID_RANDOM_BYTES_COUNT];
        ThreadLocalRandom.current().nextBytes(randomBytes);
        return new BigInteger(1, randomBytes).toString(RADIX);
    }

    public static String getExtensionByMimeType(final String type) {
        try {
            return getDefaultMimeTypes().forName(type).getExtension();
        } catch (Exception e) {
            LOGGER.warn("Can't detect extension for MIME-type {} {}", type, e);
            return "";
        }
    }

    public static String probeContentType(final Path path) {
        try (InputStream stream = newInputStream(path)) {
            return probeContentType(stream, Objects.toString(path.getFileName()));
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the mime-type of attachment {} {}", path, e);
            return "unknown";
        }
    }

    public static String probeContentType(final InputStream is, final String name) {
        try (InputStream stream = new BufferedInputStream(is)) {
            return getDefaultMimeTypes().detect(stream, METADATA).toString();
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the mime-type of attachment {} {}", name, e);
            return "unknown";
        }
    }

    public static Stream<Path> listFiles(final Path directory, final String glob) {
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, glob)) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList())
                    .stream();
        } catch (IOException e) {
            LOGGER.error("Could not list files by glob {} in directory {}: {}", glob, directory, e);
            return Stream.empty();
        }
    }

    public static Long getFileSizeSafe(final Path path) {
        try {
            return size(path);
        } catch (IOException e) {
            LOGGER.warn("Could not get the size of file {} {}", path, e);
            return null;
        }
    }
}
