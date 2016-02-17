package org.allurefw.report;

import org.apache.tika.metadata.Metadata;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.Files.newDirectoryStream;
import static org.apache.tika.mime.MimeTypes.getDefaultMimeTypes;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
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

    //TODO think about markdown
    // maybe should be moved to generator as internal plugin.
    // In this case description should be typed (type is some string, I guess)
    public static String processMarkdown(String rawText) {
        return new PegDownProcessor(Extensions.ALL + Extensions.SUPPRESS_ALL_HTML)
                .markdownToHtml(rawText);
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
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            return getDefaultMimeTypes().detect(stream, METADATA).toString();
        } catch (IOException e) {
            LOGGER.warn("Couldn't detect the mime-type of attachment {} {}", path, e);
            return "unknown";
        }
    }

    //TODO think about this file utils

    /**
     * The safe wrapper for {@link #listFilesSafe(String, Path...)}
     */
    public static List<Path> listFilesSafe(String glob, Path... directories) {
        try {
            return listFiles(glob, directories);
        } catch (IOException e) {
            LOGGER.error("Could not find any files by glob '{}': {}", glob, e);
            return Collections.emptyList();
        }
    }

    /**
     * Find all files by glob in specified directories.
     *
     * @param directories the directory to find suite files.
     * @return the list of found test suite files.
     * @throws IOException if any occurs.
     */
    public static List<Path> listFiles(String glob, Path... directories) throws IOException {
        List<Path> result = new ArrayList<>();
        for (Path directory : directories) {
            result.addAll(listFiles(glob, directory));
        }
        return result;
    }

    /**
     * Find all files by glob in specified directory.
     *
     * @param directory the directory to find suite files.
     * @return the list of found test suite files.
     * @throws IOException if any occurs.
     */
    public static List<Path> listFiles(String glob, Path directory) throws IOException {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, glob)) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    result.add(path);
                }
            }
        }
        return result;
    }
}
