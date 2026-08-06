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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for resolving and opening files below a local report root.
 */
class LocalReportFilesTest {

    /**
     * Verifies a regular report file resolves to its canonical path.
     * The test checks redundant path elements do not change the selected file.
     */
    @Description
    @Test
    void shouldResolveCanonicalFileWithinReportDirectory(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path nestedDirectory = Files.createDirectories(reportDirectory.resolve("nested"));
        final Path reportFile = Files.writeString(nestedDirectory.resolve("result.json"), "{}");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);

        assertThat(
                LocalReportFiles.resolveRequestedPath(
                        realReportDirectory,
                        "./nested/result.json"
                )
        ).contains(reportFile.toRealPath());
    }

    /**
     * Verifies canonical resolution rejects an intermediate symbolic link.
     * The test checks a link cannot select a readable file outside the report root.
     */
    @Description
    @Test
    void shouldRejectCanonicalFileBehindIntermediateSymbolicLink(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path outsideDirectory = Files.createDirectories(temp.resolve("outside"));
        Files.writeString(outsideDirectory.resolve("outside.txt"), "outside");
        createSymbolicLinkOrSkip(reportDirectory.resolve("link"), outsideDirectory);
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);

        assertThat(
                LocalReportFiles.resolveRequestedPath(
                        realReportDirectory,
                        "./link/outside.txt"
                )
        ).isEmpty();
    }

    /**
     * Verifies canonical files below data/attachments are classified as attachments.
     * The test checks classification uses the file's location below the real report root.
     */
    @Description
    @Test
    void shouldClassifyCanonicalAttachmentPath(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path attachments = Files.createDirectories(reportDirectory.resolve("data").resolve("attachments"));
        final Path attachment = Files.writeString(attachments.resolve("preview.html"), "<p>preview</p>");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);

        assertThat(
                LocalReportFiles.isAttachmentPath(
                        realReportDirectory,
                        attachment.toRealPath()
                )
        ).isTrue();
    }

    /**
     * Verifies attachment classification fails closed for case aliases.
     * The test checks a mixed-case data/attachments path receives attachment handling.
     */
    @Description
    @Test
    void shouldClassifyMixedCaseAttachmentPath(@TempDir final Path temp) throws Exception {
        final Path realReportDirectory = Files.createDirectories(temp.resolve("report")).toRealPath();
        final Path mixedCaseAttachment = realReportDirectory
                .resolve("DATA")
                .resolve("Attachments")
                .resolve("preview.html");

        assertThat(
                LocalReportFiles.isAttachmentPath(
                        realReportDirectory,
                        mixedCaseAttachment
                )
        ).isTrue();
    }

    /**
     * Verifies report application files are not classified as attachments.
     * The test checks only files below data/attachments receive attachment handling.
     */
    @Description
    @Test
    void shouldNotClassifyCanonicalReportPathAsAttachment(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("index.html"), "<p>report</p>");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);

        assertThat(
                LocalReportFiles.isAttachmentPath(
                        realReportDirectory,
                        reportFile.toRealPath()
                )
        ).isFalse();
    }

    /**
     * Verifies the final file-opening step does not follow symbolic links.
     * The test checks a symbolic-link file cannot be opened after path validation.
     */
    @Description
    @Test
    void shouldRejectOpeningSymbolicLinkFile(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path fileLink = realReportDirectory.resolve("file-link.txt");
        createSymbolicLinkOrSkip(fileLink, outsideFile.toRealPath());

        assertThat(LocalReportFiles.openFile(realReportDirectory, fileLink)).isEmpty();
        assertThat(LocalReportFiles.openFileWithPathValidation(realReportDirectory, fileLink)).isEmpty();
    }

    /**
     * Verifies the validated fallback opens regular report files.
     * The test checks the returned channel contains the exact file bytes.
     */
    @Description
    @Test
    void shouldOpenRegularFileWithPathValidationFallback(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("result.txt"), "inside");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);

        try (SeekableByteChannel channel = LocalReportFiles
                .openFileWithPathValidation(realReportDirectory, reportFile.toRealPath())
                .orElseThrow(AssertionError::new)) {
            assertThat(readChannel(channel)).isEqualTo("inside");
        }
    }

    /**
     * Verifies the validated fallback rejects files outside the report root.
     * The test checks an otherwise readable canonical file is not opened.
     */
    @Description
    @Test
    void shouldRejectOutsideFileWithPathValidationFallback(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);

        assertThat(
                LocalReportFiles.openFileWithPathValidation(
                        realReportDirectory,
                        outsideFile.toRealPath()
                )
        ).isEmpty();
    }

    /**
     * Verifies the validated fallback serves only regular existing files.
     * The test checks missing paths and directories are both rejected.
     */
    @Description
    @Test
    void shouldRejectMissingPathAndDirectoryWithPathValidationFallback(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path nestedDirectory = Files.createDirectories(reportDirectory.resolve("nested"));
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);

        assertThat(
                LocalReportFiles.openFileWithPathValidation(
                        realReportDirectory,
                        realReportDirectory.resolve("missing.txt")
                )
        ).isEmpty();
        assertThat(
                LocalReportFiles.openFileWithPathValidation(
                        realReportDirectory,
                        nestedDirectory.toRealPath()
                )
        ).isEmpty();
    }

    /**
     * Verifies serving remains bound to the file handle opened after validation.
     * The test checks replacing its directory entry with a symlink cannot redirect the opened content.
     */
    @Description
    @Test
    void shouldKeepOpenedFileBoundWhenPathIsReplaced(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("result.txt"), "inside");
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path realReportFile = reportFile.toRealPath();

        try (SeekableByteChannel channel = LocalReportFiles.openFile(realReportDirectory, realReportFile)
                .orElseThrow(AssertionError::new)) {
            replaceWithSymbolicLinkOrSkip(realReportFile, outsideFile);
            try (InputStream input = Channels.newInputStream(channel)) {
                assertThat(new String(input.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("inside");
            }
        }
    }

    /**
     * Verifies a final symlink introduced after pre-validation is rejected.
     * The test checks the fallback reaches its no-follow open rather than relying on the initial guard.
     */
    @Description
    @Test
    void shouldRejectFinalSymlinkCreatedBeforeFallbackOpen(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("result.txt"), "inside");
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside").toRealPath();
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path realReportFile = reportFile.toRealPath();
        verifySymbolicLinkSupport(realReportDirectory.resolve("probe-link"), outsideFile);

        assertThat(
                LocalReportFiles.openFileWithPathValidation(
                        realReportDirectory,
                        realReportFile,
                        () -> replaceWithSymbolicLink(realReportFile, outsideFile),
                        () -> {
                        }
                )
        ).isEmpty();
        assertThat(Files.isSymbolicLink(realReportFile)).isTrue();
    }

    /**
     * Verifies a path replacement after the fallback open fails post-open validation.
     * The test checks the opened channel is rejected before it can be returned for serving.
     */
    @Description
    @Test
    void shouldRejectFinalSymlinkCreatedAfterFallbackOpen(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("result.txt"), "inside");
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside").toRealPath();
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path realReportFile = reportFile.toRealPath();
        verifySymbolicLinkSupport(realReportDirectory.resolve("probe-link"), outsideFile);

        assertThat(
                LocalReportFiles.openFileWithPathValidation(
                        realReportDirectory,
                        realReportFile,
                        () -> {
                        },
                        () -> replaceWithSymbolicLink(realReportFile, outsideFile)
                )
        ).isEmpty();
        assertThat(Files.isSymbolicLink(realReportFile)).isTrue();
    }

    /**
     * Verifies secure opening rejects a final symlink introduced after the initial path check.
     * The test checks the component-relative byte-channel open uses no-follow semantics.
     */
    @Description
    @Test
    void shouldRejectFinalSymlinkCreatedBeforeSecureOpen(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("result.txt"), "inside");
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside").toRealPath();
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path realReportFile = reportFile.toRealPath();
        assumeSecureDirectoryStream(realReportDirectory);
        verifySymbolicLinkSupport(realReportDirectory.resolve("probe-link"), outsideFile);
        final AtomicBoolean beforeOpenInvoked = new AtomicBoolean();

        assertThat(
                LocalReportFiles.openFile(
                        realReportDirectory,
                        realReportFile,
                        () -> {
                            beforeOpenInvoked.set(true);
                            replaceWithSymbolicLink(realReportFile, outsideFile);
                        },
                        this::doNothing
                )
        ).isEmpty();
        assertThat(beforeOpenInvoked).isTrue();
        assertThat(Files.isSymbolicLink(realReportFile)).isTrue();
    }

    /**
     * Verifies secure opening rejects an intermediate symlink introduced after the initial path check.
     * The test checks component-relative directory traversal does not follow the replacement.
     */
    @Description
    @Test
    void shouldRejectIntermediateSymlinkCreatedBeforeSecureDirectoryOpen(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path nestedDirectory = Files.createDirectories(reportDirectory.resolve("nested"));
        final Path reportFile = Files.writeString(nestedDirectory.resolve("result.txt"), "inside");
        final Path outsideDirectory = Files.createDirectories(temp.resolve("outside")).toRealPath();
        Files.writeString(outsideDirectory.resolve("result.txt"), "outside");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path realNestedDirectory = nestedDirectory.toRealPath();
        final Path realReportFile = reportFile.toRealPath();
        assumeSecureDirectoryStream(realReportDirectory);
        verifySymbolicLinkSupport(realReportDirectory.resolve("probe-link"), outsideDirectory);
        final AtomicBoolean beforeDirectoryOpenInvoked = new AtomicBoolean();

        assertThat(
                LocalReportFiles.openFile(
                        realReportDirectory,
                        realReportFile,
                        () -> {
                            if (beforeDirectoryOpenInvoked.compareAndSet(false, true)) {
                                replaceDirectoryWithSymbolicLink(
                                        realNestedDirectory,
                                        realReportFile,
                                        outsideDirectory
                                );
                            }
                        },
                        this::doNothing
                )
        ).isEmpty();
        assertThat(beforeDirectoryOpenInvoked).isTrue();
        assertThat(Files.isSymbolicLink(realNestedDirectory)).isTrue();
    }

    /**
     * Verifies secure opening stays bound when the path changes after the byte channel opens.
     * The test checks the returned channel still contains the original report bytes.
     */
    @Description
    @Test
    void shouldKeepSecureFileBoundWhenPathIsReplacedAfterOpen(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("result.txt"), "inside");
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside").toRealPath();
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path realReportFile = reportFile.toRealPath();
        assumeSecureDirectoryStream(realReportDirectory);
        verifySymbolicLinkSupport(realReportDirectory.resolve("probe-link"), outsideFile);
        final AtomicBoolean afterOpenInvoked = new AtomicBoolean();

        try (SeekableByteChannel channel = LocalReportFiles.openFile(
                realReportDirectory,
                realReportFile,
                this::doNothing,
                () -> {
                    afterOpenInvoked.set(true);
                    replaceWithSymbolicLink(realReportFile, outsideFile);
                }
        ).orElseThrow(AssertionError::new)) {
            assertThat(afterOpenInvoked).isTrue();
            assertThat(Files.isSymbolicLink(realReportFile)).isTrue();
            assertThat(readChannel(channel)).isEqualTo("inside");
        }
    }

    /**
     * Verifies secure file opening passes no-follow to the byte-channel operation.
     * The test checks the exact open options used for the final path component.
     */
    @Description
    @Test
    @SuppressWarnings("unchecked")
    void shouldRequestNoFollowWhenOpeningSecureFile() throws Exception {
        final SecureDirectoryStream<Path> directoryStream = mock(SecureDirectoryStream.class);
        final SeekableByteChannel expectedChannel = mock(SeekableByteChannel.class);
        final Path relativeFile = Path.of("result.txt");
        when(directoryStream.newByteChannel(eq(relativeFile), anySet())).thenReturn(expectedChannel);

        final SeekableByteChannel openedChannel = LocalReportFiles.openSecureFile(
                directoryStream,
                relativeFile,
                this::doNothing,
                this::doNothing
        );
        final ArgumentCaptor<Set<? extends OpenOption>> options = ArgumentCaptor.forClass(Set.class);
        verify(directoryStream).newByteChannel(eq(relativeFile), options.capture());
        final Set<OpenOption> capturedOptions = new HashSet<>(options.getValue());

        assertThat(openedChannel).isSameAs(expectedChannel);
        assertThat(capturedOptions)
                .contains(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Verifies secure directory traversal passes no-follow to each intermediate open.
     * The test checks the child stream is opened without following a replacement link.
     */
    @Description
    @Test
    @SuppressWarnings("unchecked")
    void shouldRequestNoFollowWhenOpeningSecureDirectory() throws Exception {
        final SecureDirectoryStream<Path> reportDirectoryStream = mock(SecureDirectoryStream.class);
        final SecureDirectoryStream<Path> nestedDirectoryStream = mock(SecureDirectoryStream.class);
        final SecureDirectoryStream<Path> deeperDirectoryStream = mock(SecureDirectoryStream.class);
        final SeekableByteChannel expectedChannel = mock(SeekableByteChannel.class);
        final Path nestedDirectory = Path.of("nested");
        final Path deeperDirectory = Path.of("deeper");
        final Path relativeFile = nestedDirectory.resolve(deeperDirectory).resolve("result.txt");
        when(
                reportDirectoryStream.newDirectoryStream(
                        eq(nestedDirectory),
                        eq(LinkOption.NOFOLLOW_LINKS)
                )
        ).thenReturn(nestedDirectoryStream);
        when(
                nestedDirectoryStream.newDirectoryStream(
                        eq(deeperDirectory),
                        eq(LinkOption.NOFOLLOW_LINKS)
                )
        ).thenReturn(deeperDirectoryStream);
        when(
                deeperDirectoryStream.newByteChannel(
                        eq(relativeFile.getFileName()),
                        anySet()
                )
        ).thenReturn(expectedChannel);

        final SeekableByteChannel openedChannel = LocalReportFiles.openSecureFile(
                reportDirectoryStream,
                relativeFile,
                this::doNothing,
                this::doNothing
        );

        assertThat(openedChannel).isSameAs(expectedChannel);
        verify(reportDirectoryStream).newDirectoryStream(
                nestedDirectory,
                LinkOption.NOFOLLOW_LINKS
        );
        verify(nestedDirectoryStream).newDirectoryStream(
                deeperDirectory,
                LinkOption.NOFOLLOW_LINKS
        );
        verify(reportDirectoryStream, never()).close();
        verify(nestedDirectoryStream).close();
        verify(deeperDirectoryStream).close();
    }

    /**
     * Verifies the validated fallback stays bound to the opened file handle.
     * The test checks replacing the directory entry cannot redirect the returned channel.
     */
    @Description
    @Test
    void shouldKeepFallbackFileBoundWhenPathIsReplaced(@TempDir final Path temp) throws Exception {
        final Path reportDirectory = Files.createDirectories(temp.resolve("report"));
        final Path reportFile = Files.writeString(reportDirectory.resolve("result.txt"), "inside");
        final Path outsideFile = Files.writeString(temp.resolve("outside.txt"), "outside");
        final Path realReportDirectory = LocalReportFiles.resolveReportDirectory(reportDirectory);
        final Path realReportFile = reportFile.toRealPath();

        try (SeekableByteChannel channel = LocalReportFiles
                .openFileWithPathValidation(realReportDirectory, realReportFile)
                .orElseThrow(AssertionError::new)) {
            replaceWithSymbolicLinkOrSkip(realReportFile, outsideFile);
            assertThat(readChannel(channel)).isEqualTo("inside");
        }
    }

    private String readChannel(final SeekableByteChannel channel) throws IOException {
        try (InputStream input = Channels.newInputStream(channel)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void createSymbolicLinkOrSkip(final Path link, final Path target) throws IOException {
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | IOException e) {
            throw new TestAbortedException("Symbolic links are not supported in this environment", e);
        }
    }

    private void replaceWithSymbolicLinkOrSkip(final Path file, final Path target) throws IOException {
        try {
            replaceWithSymbolicLink(file, target);
        } catch (UnsupportedOperationException | IOException e) {
            throw new TestAbortedException("Open files cannot be replaced in this environment", e);
        }
    }

    private void replaceWithSymbolicLink(final Path file, final Path target) throws IOException {
        Files.delete(file);
        Files.createSymbolicLink(file, target);
    }

    private void replaceDirectoryWithSymbolicLink(final Path directory,
                                                  final Path containedFile,
                                                  final Path target)
            throws IOException {
        Files.delete(containedFile);
        Files.delete(directory);
        Files.createSymbolicLink(directory, target);
    }

    private void verifySymbolicLinkSupport(final Path link, final Path target) throws IOException {
        createSymbolicLinkOrSkip(link, target);
        Files.delete(link);
    }

    private void assumeSecureDirectoryStream(final Path directory) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            assumeTrue(
                    directoryStream instanceof SecureDirectoryStream,
                    "SecureDirectoryStream is not supported by the test file system"
            );
        }
    }

    private void doNothing() {
    }
}
