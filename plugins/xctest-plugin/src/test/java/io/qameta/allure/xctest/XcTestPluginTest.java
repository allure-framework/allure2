/*
 *  Copyright 2016-2024 Qameta Software Inc
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
import io.qameta.allure.entity.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    void shouldSetTestStartAndStop() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sample.plist")) {
            Files.copy(Objects.requireNonNull(is), resultsDirectory.resolve("sample.plist"));
        }

        new XcTestPlugin().readResults(configuration, visitor, resultsDirectory);

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(14))
                .visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
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
