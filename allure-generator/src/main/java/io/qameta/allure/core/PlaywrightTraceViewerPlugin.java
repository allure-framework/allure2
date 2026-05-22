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

import io.qameta.allure.Aggregator2;
import io.qameta.allure.ReportGenerationException;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Plugin that stores Playwright Trace Viewer assets when the report contains
 * Playwright trace attachments.
 */
public class PlaywrightTraceViewerPlugin implements Aggregator2 {

    static final String PLAYWRIGHT_TRACE_MIME_TYPE = "application/vnd.allure.playwright-trace";
    static final String PLAYWRIGHT_TRACE_VIEWER_URL = "playwright-trace-viewer/index.html";
    static final String PLAYWRIGHT_TRACE_VIEWER_INFO_JSON = "data/playwright-trace-viewer.json";

    private static final String PLAYWRIGHT_TRACE_VIEWER_ASSETS_JSON = "playwright-trace-viewer-assets.json";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        if (storage instanceof InMemoryReportStorage || !hasAnyPlaywrightTraces(launchesResults)) {
            return;
        }

        StaticAssetManifest.load(PLAYWRIGHT_TRACE_VIEWER_ASSETS_JSON)
                .forEach(resourceName -> storage.addDataBinary(resourceName, readResource(resourceName)));
        storage.addDataJson(
                PLAYWRIGHT_TRACE_VIEWER_INFO_JSON,
                Collections.singletonMap("url", PLAYWRIGHT_TRACE_VIEWER_URL)
        );
    }

    static boolean hasAnyPlaywrightTraces(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .flatMap(launch -> launch.getAllResults().stream())
                .anyMatch(PlaywrightTraceViewerPlugin::hasTraceInResult);
    }

    private static boolean hasTraceInResult(final TestResult result) {
        return hasTraceInStages(result.getBeforeStages())
                || hasTraceInStage(result.getTestStage())
                || hasTraceInStages(result.getAfterStages());
    }

    private static boolean hasTraceInStages(final List<StageResult> stages) {
        return stages != null && stages.stream().anyMatch(PlaywrightTraceViewerPlugin::hasTraceInStage);
    }

    private static boolean hasTraceInStage(final StageResult stage) {
        return stage != null
                && (hasTraceInAttachments(stage.getAttachments())
                        || hasTraceInSteps(stage.getSteps()));
    }

    private static boolean hasTraceInSteps(final List<Step> steps) {
        return steps != null && steps.stream().anyMatch(PlaywrightTraceViewerPlugin::hasTraceInStep);
    }

    private static boolean hasTraceInStep(final Step step) {
        return step != null
                && (hasTraceInAttachments(step.getAttachments())
                        || hasTraceInSteps(step.getSteps()));
    }

    private static boolean hasTraceInAttachments(final List<Attachment> attachments) {
        return attachments != null && attachments.stream().anyMatch(PlaywrightTraceViewerPlugin::isTraceAttachment);
    }

    private static boolean isTraceAttachment(final Attachment attachment) {
        return attachment != null && PLAYWRIGHT_TRACE_MIME_TYPE.equals(attachment.getType());
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    private static byte[] readResource(final String resourceName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (Objects.isNull(is)) {
                throw new ReportGenerationException(String.format("Resource %s not found", resourceName));
            }
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new ReportGenerationException("Can't read resource " + resourceName, e);
        }
    }

}
