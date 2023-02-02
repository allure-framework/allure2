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
package io.qameta.allure.xunitxml;

import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Label;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class XunitXmlPluginTest {

    private Configuration configuration;
    private ResultsVisitor visitor;
    private Path resultsDirectory;

    @BeforeEach
    void setUp(@TempDir final Path resultsDirectory) {
        configuration = mock(Configuration.class);
        when(configuration.requireContext(RandomUidContext.class)).thenReturn(new RandomUidContext());
        visitor = mock(ResultsVisitor.class);
        this.resultsDirectory = resultsDirectory;
    }

    @Test
    void shouldCreateTest() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getName, TestResult::getHistoryId, TestResult::getStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("passedTest", "Some test", Status.PASSED)
                );
    }

    @Test
    void shouldSetTime() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getTime)
                .extracting(Time::getDuration)
                .containsExactlyInAnyOrder(44L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSetLabels() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .extracting(Label::getName, Label::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LabelName.SUITE.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.PACKAGE.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.TEST_CLASS.value(), "org.example.XunitTest"),
                        Tuple.tuple(LabelName.RESULT_FORMAT.value(), XunitXmlPlugin.XUNIT_RESULTS_FORMAT)
                );
    }

    @Test
    void shouldSetFullName() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getFullName)
                .containsExactlyInAnyOrder(
                        "Some test"
                );
    }

    @Test
    void shouldSetFramework() throws Exception {
        process(
                "xunitdata/framework-test.xml",
                "passed-test.xml"
        );

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .flatExtracting(TestResult::getLabels)
                .filteredOn(label -> label.getName().equals(LabelName.FRAMEWORK.value()))
                .extracting(Label::getValue)
                .containsExactly("junit");
    }

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("xunitdata/failed-test.xml", "failed-test.xml",
                        String.format("%s%n", "Assert.True() Failure\\r\\nExpected: True\\r\\nActual:   False") +
                                "test output\\n", "FAILED-TRACE"),
                Arguments.of("xunitdata/passed-test.xml", "passed-test.xml", "test output\\n", null)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldSetStatusDetails(final String resource,
                                final String fileName,
                                final String message,
                                final String trace) throws Exception {
        process(resource, fileName);

        final ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        verify(visitor, times(1)).visitTestResult(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(TestResult::getStatusMessage, TestResult::getStatusTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(message, trace)
                );
    }

    private void process(String... strings) throws IOException {
        Iterator<String> iterator = Arrays.asList(strings).iterator();
        while (iterator.hasNext()) {
            String first = iterator.next();
            String second = iterator.next();
            copyFile(resultsDirectory, first, second);
        }
        XunitXmlPlugin reader = new XunitXmlPlugin();

        reader.readResults(configuration, visitor, resultsDirectory);
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(Objects.requireNonNull(is), dir.resolve(fileName));
        }
    }

}
