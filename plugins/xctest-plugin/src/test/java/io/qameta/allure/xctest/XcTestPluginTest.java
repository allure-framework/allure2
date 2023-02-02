/*
 *  Copyright 2016-2023 Qameta Software OÜ
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

import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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

    @Test
    void shouldParseResults() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sample.plist")) {
            Files.copy(Objects.requireNonNull(is), resultsDirectory.resolve("sample.plist"));
        }

        new XcTestPlugin().readResults(configuration, visitor, resultsDirectory);

        verify(visitor, times(14))
                .visitTestResult(any(TestResult.class));
    }

    @Test
    public void shouldParseHasScreenShotData() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("has-screenshot-data.plist")) {
            Files.copy(Objects.requireNonNull(is), resultsDirectory.resolve("sample.plist"));
        }
        final Path attachments = resultsDirectory.resolve("Attachments");
        Files.createDirectories(attachments);

        final Path screenshot = attachments.resolve("Screenshot_92D015E5-965D-4171-849C-35CC0945FEA2.png");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("screenshot.png")) {
            Files.copy(Objects.requireNonNull(is), screenshot);
        }

        new XcTestPlugin().readResults(configuration, visitor, resultsDirectory);

        verify(visitor, times(1))
                .visitAttachmentFile(screenshot);
    }

    @Test
    public void shouldParseAttachmentsData() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("attachments-data.plist")) {
            Files.copy(Objects.requireNonNull(is), resultsDirectory.resolve("sample.plist"));
        }
        final Path attachments = resultsDirectory.resolve("Attachments");
        Files.createDirectories(attachments);

        final Path screenshot = attachments.resolve("Screenshot_1_1FBB627A-3D11-41E3-B4E6-5C717C75F175.jpeg");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("screenshot.png")) {
            Files.copy(Objects.requireNonNull(is), screenshot);
        }

        new XcTestPlugin().readResults(configuration, visitor, resultsDirectory);

        verify(visitor, times(1))
                .visitAttachmentFile(screenshot);
    }
}
