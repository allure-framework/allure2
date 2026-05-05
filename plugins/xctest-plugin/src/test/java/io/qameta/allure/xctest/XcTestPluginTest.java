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
package io.qameta.allure.xctest;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class XcTestPluginTest {

    private Configuration configuration;
    private ResultsVisitor visitor;
    private Path resultsDirectory;

    @BeforeEach
    void setUp(@TempDir final Path resultsDirectory) {
        configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class)).thenReturn(new JacksonContext());
        visitor = mock(ResultsVisitor.class);
        this.resultsDirectory = resultsDirectory;
    }

    /**
     * Verifies an XCTest plist is parsed into Allure test results.
     * The test checks the number of emitted results from the sample report.
     */
    @Description
    @Test
    void shouldParseResults() throws Exception {
        copyResource(resultsDirectory, "sample.plist", "sample.plist");

        readResults(resultsDirectory);

        final List<TestResult> results = captureTestResults(14);

        assertThat(results)
                .hasSize(14);
    }

    /**
     * Verifies XCTest start and stop times are converted into Allure time fields.
     * The test checks the exact parsed time window for each sample test.
     */
    @Description
    @Test
    void shouldSetTestStartAndStop() throws Exception {
        copyResource(resultsDirectory, "sample.plist", "sample.plist");

        readResults(resultsDirectory);

        final List<TestResult> results = captureTestResults(14);

        assertThat(results)
                .extracting(TestResult::getName, TestResult::getTime)
                .contains(
                        tuple("test_C1433()", Time.create(1494595000L, 1494626548L)),
                        tuple("test_C1400()", Time.create(1494595031L, 1494621269L)),
                        tuple("test_C1401()", Time.create(1494595057L, 1494623087L)),
                        tuple("test_C1394()", Time.create(1494595085L, 1494623215L)),
                        tuple("test_C7096()", Time.create(1494595114L, 1494619303L)),
                        tuple("test_C1395()", Time.create(1494595138L, 1494626076L)),
                        tuple("test_C1474()", Time.create(1494595169L, 1494625148L)),
                        tuple("test_C1396()", Time.create(1494595199L, 1494626546L)),
                        tuple("test_C6923()", Time.create(1494595262L, 1494624579L)),
                        tuple("test_C1398()", Time.create(1494595292L, 1494629469L)),
                        tuple("test_C6924()", Time.create(1494595326L, 1494619613L)),
                        tuple("test_C1399()", Time.create(1494595350L, 1494642300L)),
                        tuple("test_C1397()", Time.create(1494595230L, 1494627643L)),
                        tuple("test_C6925()", Time.create(1494595397L, 1494624364L))
                );
    }

    /**
     * Verifies XCTest screenshot references are sent to the attachment visitor.
     * The test checks the screenshot file from the plist is visited once.
     */
    @Description
    @Test
    public void shouldParseHasScreenShotData() throws Exception {
        copyResource(resultsDirectory, "has-screenshot-data.plist", "sample.plist");
        final Path attachments = resultsDirectory.resolve("Attachments");
        Files.createDirectories(attachments);

        final Path screenshot = copyResource(attachments, "screenshot.png",
                "Screenshot_92D015E5-965D-4171-849C-35CC0945FEA2.png");

        readResults(resultsDirectory);

        assertThat(captureAttachmentFiles(1))
                .containsExactly(screenshot);
    }

    /**
     * Verifies XCTest screenshots resolve when the results path is relative.
     * The test checks the normalized screenshot file is visited once.
     */
    @Description
    @Test
    public void shouldAddScreenshotsWhenRelativeResultsDir() throws Exception {
        final Path xctestResults = resultsDirectory.resolve("xctest-results");
        Files.createDirectories(xctestResults);
        copyResource(xctestResults, "has-screenshot-data.plist", "sample.plist");
        final Path attachments = xctestResults.resolve("Attachments");
        Files.createDirectories(attachments);

        final Path screenshot = copyResource(attachments, "screenshot.png",
                "Screenshot_92D015E5-965D-4171-849C-35CC0945FEA2.png");

        final Path relative = xctestResults.resolve("..").resolve("xctest-results");
        readResults(relative);

        assertThat(captureAttachmentFiles(1))
                .containsExactly(screenshot);
    }

    /**
     * Verifies screenshot paths cannot escape the XCTest results directory.
     * The test checks a traversal plist does not send any attachment file to the visitor.
     */
    @Description
    @Test
    public void shouldNotAllowPathTraversalForScreenshots() throws Exception {
        copyResource(resultsDirectory, "has-screenshot-data-path-traversal.plist", "sample.plist");
        final Path attachments = resultsDirectory.resolve("Attachments");
        Files.createDirectories(attachments);

        Files.createDirectories(attachments.resolve("Screenshot_"));

        copyResource(resultsDirectory, "screenshot.png", "secret-file.png");

        readResults(resultsDirectory);

        verifyNoAttachmentFiles();
    }

    /**
     * Verifies generic attachment paths cannot escape the XCTest results directory.
     * The test checks a traversal plist does not visit the secret attachment file.
     */
    @Description
    @Test
    public void shouldNotAllowPathTraversalForAttachments() throws Exception {
        copyResource(resultsDirectory, "attachments-data-path-traversal.plist", "sample.plist");
        final Path attachments = resultsDirectory.resolve("Attachments");
        Files.createDirectories(attachments);

        final Path secretFile = copyResource(resultsDirectory, "screenshot.png", "secret-file.png");

        readResults(resultsDirectory);

        verifyAttachmentFileWasNotVisited(secretFile);
    }

    /**
     * Verifies XCTest attachment records are sent to the attachment visitor.
     * The test checks the referenced JPEG file is visited once.
     */
    @Description
    @Test
    public void shouldParseAttachmentsData() throws Exception {
        copyResource(resultsDirectory, "attachments-data.plist", "sample.plist");
        final Path attachments = resultsDirectory.resolve("Attachments");
        Files.createDirectories(attachments);

        final Path screenshot = copyResource(attachments, "screenshot.png",
                "Screenshot_1_1FBB627A-3D11-41E3-B4E6-5C717C75F175.jpeg");

        readResults(resultsDirectory);

        assertThat(captureAttachmentFiles(1))
                .containsExactly(screenshot);
    }

    /**
     * Verifies XCTest attachment records resolve when the results path is relative.
     * The test checks the normalized JPEG file is visited once.
     */
    @Description
    @Test
    public void shouldAllureAttachmentsDataWithRelativeResultsDir() throws Exception {
        final Path xctestResults = resultsDirectory.resolve("xctest-results");
        Files.createDirectories(xctestResults);
        copyResource(xctestResults, "attachments-data.plist", "sample.plist");
        final Path attachments = xctestResults.resolve("Attachments");
        Files.createDirectories(attachments);

        final Path screenshot = copyResource(attachments, "screenshot.png",
                "Screenshot_1_1FBB627A-3D11-41E3-B4E6-5C717C75F175.jpeg");
        final Path relative = xctestResults.resolve("..").resolve("xctest-results");
        readResults(relative);

        assertThat(captureAttachmentFiles(1))
                .containsExactly(screenshot);
    }

    private void readResults(final Path directory) {
        Allure.step("Read XCTest results", () -> new XcTestPlugin().readResults(configuration, visitor, directory));
    }

    private Path copyResource(final Path directory, final String resourceName, final String fileName) throws IOException {
        return Allure.step("Copy fixture resource " + resourceName + " as " + fileName, () -> {
            final byte[] content;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                content = Objects.requireNonNull(is).readAllBytes();
            }
            final Path destination = directory.resolve(fileName);
            Files.createDirectories(destination.getParent());
            Files.write(destination, content);
            attachResourceContent(fileName, content);
            return destination;
        });
    }

    private List<TestResult> captureTestResults(final int expectedCount) {
        return Allure.step("Capture parsed XCTest test results", () -> {
            final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(visitor, times(expectedCount)).visitTestResult(captor.capture());
            final List<TestResult> results = captor.getAllValues();
            Allure.addAttachment("parsed-test-results.txt", "text/plain", describeTestResults(results));
            return results;
        });
    }

    private List<Path> captureAttachmentFiles(final int expectedCount) {
        return Allure.step("Capture visited XCTest attachment files", () -> {
            final ArgumentCaptor<Path> captor = ArgumentCaptor.forClass(Path.class);
            verify(visitor, times(expectedCount)).visitAttachmentFile(captor.capture());
            final List<Path> attachments = captor.getAllValues();
            Allure.addAttachment("visited-attachments.txt", "text/plain", describeAttachments(attachments));
            return attachments;
        });
    }

    private void verifyNoAttachmentFiles() {
        Allure.step("Verify no XCTest attachment files were visited", () -> {
            verify(visitor, times(0)).visitAttachmentFile(any());
            Allure.addAttachment("visited-attachments.txt", "text/plain", "attachments=0");
        });
    }

    private void verifyAttachmentFileWasNotVisited(final Path attachment) {
        Allure.step("Verify XCTest attachment file was not visited", () -> {
            verify(visitor, times(0)).visitAttachmentFile(attachment);
            Allure.addAttachment("visited-attachments.txt", "text/plain",
                    "attachments=0" + System.lineSeparator() + "blockedFile=" + attachment.getFileName());
        });
    }

    private void attachResourceContent(final String fileName, final byte[] content) {
        if (fileName.endsWith(".plist")) {
            Allure.addAttachment(fileName, "application/xml", new String(content, StandardCharsets.UTF_8), ".plist");
            return;
        }
        final String type = fileName.endsWith(".jpeg") ? "image/jpeg" : "image/png";
        final String extension = fileName.endsWith(".jpeg") ? ".jpeg" : ".png";
        Allure.addAttachment(fileName, type, new ByteArrayInputStream(content), extension);
    }

    private String describeTestResults(final List<TestResult> results) {
        final StringBuilder builder = new StringBuilder();
        builder.append("results=").append(results.size()).append(System.lineSeparator());
        results.forEach(result -> builder
                .append(System.lineSeparator())
                .append("name=").append(result.getName()).append(System.lineSeparator())
                .append("time=").append(describeTime(result.getTime())).append(System.lineSeparator())
        );
        return builder.toString();
    }

    private String describeAttachments(final List<Path> attachments) {
        final StringBuilder builder = new StringBuilder();
        builder.append("attachments=").append(attachments.size()).append(System.lineSeparator());
        attachments.forEach(attachment -> builder
                .append(System.lineSeparator())
                .append("file=").append(attachment.getFileName()).append(System.lineSeparator())
                .append("size=").append(sizeOf(attachment)).append(System.lineSeparator())
        );
        return builder.toString();
    }

    private long sizeOf(final Path attachment) {
        try {
            return Files.size(attachment);
        } catch (IOException e) {
            return -1L;
        }
    }

    private String describeTime(final Time time) {
        if (time == null) {
            return null;
        }
        return String.format("start=%s, stop=%s, duration=%s", time.getStart(), time.getStop(), time.getDuration());
    }
}
