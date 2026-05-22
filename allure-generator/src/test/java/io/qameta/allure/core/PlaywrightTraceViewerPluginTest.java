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
package io.qameta.allure.core;

import io.qameta.allure.Allure;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.Description;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaywrightTraceViewerPluginTest {

    private final PlaywrightTraceViewerPlugin plugin = new PlaywrightTraceViewerPlugin();

    /**
     * Verifies copying Playwright Trace Viewer assets and publishing the report
     * runtime info when a launch contains a Playwright trace attachment.
     */
    @Description
    @Test
    void shouldWriteViewerAssetsAndInfoForDirectoryReportsWithTraces(@TempDir final Path tempDirectory)
            throws Exception {
        plugin.aggregate(
                ConfigurationBuilder.empty().build(),
                launchResults(traceAttachment()),
                new FileSystemReportStorage(tempDirectory)
        );

        final Path viewerIndex = tempDirectory.resolve("playwright-trace-viewer/index.html");
        final Path viewerInfo = tempDirectory.resolve(PlaywrightTraceViewerPlugin.PLAYWRIGHT_TRACE_VIEWER_INFO_JSON);
        Allure.addAttachment(
                "Playwright trace viewer info",
                "application/json",
                Files.readString(viewerInfo, StandardCharsets.UTF_8)
        );

        assertThat(viewerIndex)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .contains("Playwright Trace Viewer");
        assertThat(tempDirectory.resolve("playwright-trace-viewer/sw.bundle.js")).isRegularFile();
        assertThat(viewerInfo)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .contains("\"url\":\"playwright-trace-viewer/index.html\"");
    }

    /**
     * Verifies skipping Playwright Trace Viewer assets when the launch has no
     * Playwright trace attachments.
     */
    @Description
    @Test
    void shouldSkipViewerAssetsForDirectoryReportsWithoutTraces(@TempDir final Path tempDirectory) {
        plugin.aggregate(
                ConfigurationBuilder.empty().build(),
                launchResults(new Attachment().setName("plain.txt").setType("text/plain")),
                new FileSystemReportStorage(tempDirectory)
        );

        assertThat(tempDirectory.resolve("playwright-trace-viewer")).doesNotExist();
        assertThat(tempDirectory.resolve(PlaywrightTraceViewerPlugin.PLAYWRIGHT_TRACE_VIEWER_INFO_JSON))
                .doesNotExist();
    }

    /**
     * Verifies keeping single-file reports self-contained even when they contain
     * Playwright trace attachments.
     */
    @Description
    @Test
    void shouldSkipViewerAssetsForSingleFileReportsWithTraces() {
        final InMemoryReportStorage storage = new InMemoryReportStorage();

        plugin.aggregate(
                ConfigurationBuilder.empty().build(),
                launchResults(traceAttachment()),
                storage
        );

        assertThat(storage.getReportDataFiles()).isEmpty();
    }

    private static Attachment traceAttachment() {
        return new Attachment()
                .setName("trace.zip")
                .setType(PlaywrightTraceViewerPlugin.PLAYWRIGHT_TRACE_MIME_TYPE);
    }

    private static List<LaunchResults> launchResults(final Attachment attachment) {
        final Step childStep = new Step().setAttachments(Collections.singletonList(attachment));
        final Step parentStep = new Step().setSteps(Collections.singletonList(childStep));
        final StageResult stage = new StageResult().setSteps(Collections.singletonList(parentStep));
        final TestResult result = new TestResult()
                .setUid("trace-test")
                .setName("opens playwright trace")
                .setTestStage(stage);

        return Collections.singletonList(
                new DefaultLaunchResults(
                        Collections.singleton(result),
                        Collections.emptyMap(),
                        Collections.emptyMap()
                )
        );
    }

}
