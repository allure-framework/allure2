/*
 *  Copyright 2016-2026 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves and classifies files served by the local report preview server.
 */
final class LocalReportFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalReportFiles.class);

    private static final String DATA_DIRECTORY = "data";
    private static final String ATTACHMENTS_DIRECTORY = "attachments";
    private static final Set<OpenOption> READ_WITHOUT_FOLLOWING_LINKS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS))
    );
    private static final Set<OpenOption> READ = Collections.singleton(StandardOpenOption.READ);

    private LocalReportFiles() {
        throw new IllegalStateException("Do not instantiate");
    }

    static Path resolveReportDirectory(final Path reportDirectory) throws IOException {
        final Path absoluteReportDirectory = reportDirectory.toAbsolutePath();
        if (Files.notExists(reportDirectory)) {
            throw new IOException("Report directory does not exist: " + absoluteReportDirectory);
        }
        if (!Files.isDirectory(reportDirectory)) {
            throw new IOException("Report path is not a directory: " + absoluteReportDirectory);
        }
        return reportDirectory.toRealPath();
    }

    static Optional<Path> resolveRequestedPath(final Path realReportDirectory,
                                               final String requestedPath) {
        try {
            final Path resolvedPath = realReportDirectory.resolve(requestedPath).normalize();
            return resolveRequestedPath(realReportDirectory, resolvedPath);
        } catch (InvalidPathException e) {
            LOGGER.debug("Could not parse requested report path {}", requestedPath, e);
            return Optional.empty();
        }
    }

    private static Optional<Path> resolveRequestedPath(final Path realReportDirectory,
                                                       final Path requestedPath) {
        if (isRejectedPath(realReportDirectory, requestedPath)) {
            return Optional.empty();
        }
        try {
            final Path realRequestedPath = requestedPath.toRealPath();
            if (!isWithinDirectory(realReportDirectory, realRequestedPath)) {
                LOGGER.debug(
                        "Rejected requested report path {} because it resolves outside {}",
                        requestedPath,
                        realReportDirectory
                );
                return Optional.empty();
            }
            return Optional.of(realRequestedPath);
        } catch (IOException e) {
            LOGGER.debug("Could not resolve requested report path {}", requestedPath, e);
            return Optional.empty();
        }
    }

    static boolean isAttachmentPath(final Path realReportDirectory,
                                    final Path realRequestedPath) {
        if (!isWithinDirectory(realReportDirectory, realRequestedPath)) {
            return false;
        }
        final Path relativePath = realReportDirectory.relativize(realRequestedPath);
        return relativePath.getNameCount() >= 3
                && DATA_DIRECTORY.equalsIgnoreCase(relativePath.getName(0).toString())
                && ATTACHMENTS_DIRECTORY.equalsIgnoreCase(relativePath.getName(1).toString());
    }

    static Optional<SeekableByteChannel> openFile(final Path realReportDirectory,
                                                  final Path realRequestedPath) {
        return openFile(
                realReportDirectory,
                realRequestedPath,
                LocalReportFiles::doNothing,
                LocalReportFiles::doNothing
        );
    }

    @SuppressWarnings("unchecked")
    static Optional<SeekableByteChannel> openFile(final Path realReportDirectory,
                                                  final Path realRequestedPath,
                                                  final FileOpenHook beforeSecureComponentOpen,
                                                  final FileOpenHook afterSecureFileOpen) {
        if (isRejectedPath(realReportDirectory, realRequestedPath)) {
            return Optional.empty();
        }
        SeekableByteChannel openedFile = null;
        try (DirectoryStream<Path> reportDirectoryStream = Files.newDirectoryStream(realReportDirectory)) {
            if (reportDirectoryStream instanceof SecureDirectoryStream) {
                openedFile = openSecureFile(
                        (SecureDirectoryStream<Path>) reportDirectoryStream,
                        realReportDirectory.relativize(realRequestedPath),
                        beforeSecureComponentOpen,
                        afterSecureFileOpen
                );
            }
        } catch (IOException | RuntimeException e) {
            closeChannel(openedFile);
            LOGGER.debug("Could not securely open requested report file {}", realRequestedPath, e);
            return Optional.empty();
        }
        if (openedFile != null) {
            return Optional.of(openedFile);
        }
        return openFileWithPathValidation(realReportDirectory, realRequestedPath);
    }

    static boolean isWithinDirectory(final Path directory,
                                     final Path requestedPath) {
        return requestedPath.startsWith(directory);
    }

    private static boolean isRejectedPath(final Path realReportDirectory,
                                          final Path requestedPath) {
        if (!isWithinDirectory(realReportDirectory, requestedPath)) {
            LOGGER.debug(
                    "Rejected requested report path {} because it is outside {}",
                    requestedPath,
                    realReportDirectory
            );
            return true;
        }
        final Optional<Path> symbolicLink = findSymbolicLink(realReportDirectory, requestedPath);
        if (symbolicLink.isPresent()) {
            LOGGER.debug(
                    "Rejected requested report path {} because it contains symbolic link {}",
                    requestedPath,
                    symbolicLink.get()
            );
            return true;
        }
        return false;
    }

    private static Optional<Path> findSymbolicLink(final Path realReportDirectory,
                                                   final Path requestedPath) {
        Path currentPath = realReportDirectory;
        for (Path pathComponent : realReportDirectory.relativize(requestedPath)) {
            currentPath = currentPath.resolve(pathComponent);
            if (Files.isSymbolicLink(currentPath)) {
                return Optional.of(currentPath);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("PMD.CloseResource") // Child streams are transferred between iterations and closed below.
    static SeekableByteChannel openSecureFile(final SecureDirectoryStream<Path> reportDirectoryStream,
                                              final Path relativeFile,
                                              final FileOpenHook beforeOpen,
                                              final FileOpenHook afterFileOpen)
            throws IOException {
        SecureDirectoryStream<Path> currentDirectoryStream = reportDirectoryStream;
        boolean closeCurrentDirectoryStream = false;
        SeekableByteChannel openedFile = null;
        try {
            for (int index = 0; index < relativeFile.getNameCount() - 1; index++) {
                beforeOpen.run();
                final SecureDirectoryStream<Path> nextDirectoryStream = currentDirectoryStream.newDirectoryStream(
                        relativeFile.getName(index),
                        LinkOption.NOFOLLOW_LINKS
                );
                try {
                    if (closeCurrentDirectoryStream) {
                        currentDirectoryStream.close();
                        closeCurrentDirectoryStream = false;
                    }
                } catch (IOException | RuntimeException e) {
                    closeDirectoryStream(nextDirectoryStream);
                    throw e;
                }
                currentDirectoryStream = nextDirectoryStream;
                closeCurrentDirectoryStream = true;
            }
            beforeOpen.run();
            openedFile = currentDirectoryStream.newByteChannel(
                    relativeFile.getFileName(),
                    READ_WITHOUT_FOLLOWING_LINKS
            );
            afterFileOpen.run();
            if (closeCurrentDirectoryStream) {
                currentDirectoryStream.close();
            }
            return openedFile;
        } catch (IOException | RuntimeException e) {
            closeChannel(openedFile);
            if (closeCurrentDirectoryStream) {
                closeDirectoryStream(currentDirectoryStream);
            }
            throw e;
        }
    }

    static Optional<SeekableByteChannel> openFileWithPathValidation(final Path realReportDirectory,
                                                                    final Path realRequestedPath) {
        return openFileWithPathValidation(
                realReportDirectory,
                realRequestedPath,
                LocalReportFiles::doNothing,
                LocalReportFiles::doNothing
        );
    }

    static Optional<SeekableByteChannel> openFileWithPathValidation(final Path realReportDirectory,
                                                                    final Path realRequestedPath,
                                                                    final FileOpenHook beforeOpen,
                                                                    final FileOpenHook afterOpen) {
        if (isRejectedPath(realReportDirectory, realRequestedPath)
                || !Files.isRegularFile(realRequestedPath, LinkOption.NOFOLLOW_LINKS)) {
            return Optional.empty();
        }
        SeekableByteChannel channel = null;
        try {
            beforeOpen.run();
            channel = openWithoutFollowingFinalLink(realRequestedPath);
            afterOpen.run();
            final Path pathAfterOpen = realRequestedPath.toRealPath();
            if (!isWithinDirectory(realReportDirectory, pathAfterOpen)
                    || !realRequestedPath.equals(pathAfterOpen)
                    || isRejectedPath(realReportDirectory, realRequestedPath)
                    || !Files.isRegularFile(realRequestedPath, LinkOption.NOFOLLOW_LINKS)) {
                closeChannel(channel);
                return Optional.empty();
            }
            return Optional.of(channel);
        } catch (IOException | UnsupportedOperationException e) {
            closeChannel(channel);
            LOGGER.debug("Could not open requested report file {}", realRequestedPath, e);
            return Optional.empty();
        } catch (RuntimeException e) {
            closeChannel(channel);
            throw e;
        }
    }

    private static SeekableByteChannel openWithoutFollowingFinalLink(final Path file) throws IOException {
        try {
            return Files.newByteChannel(file, READ_WITHOUT_FOLLOWING_LINKS);
        } catch (UnsupportedOperationException e) {
            LOGGER.debug(
                    "File system does not support opening {} with NOFOLLOW_LINKS; using validated fallback",
                    file,
                    e
            );
            return Files.newByteChannel(file, READ);
        }
    }

    private static void closeChannel(final SeekableByteChannel channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.debug("Could not close report file channel", e);
        }
    }

    private static void closeDirectoryStream(final DirectoryStream<Path> directoryStream) {
        try {
            directoryStream.close();
        } catch (IOException | RuntimeException e) {
            LOGGER.debug("Could not close report directory stream", e);
        }
    }

    private static void doNothing() {
        // Default hook for production file opening.
    }

    @FunctionalInterface
    interface FileOpenHook {

        void run() throws IOException;
    }
}
