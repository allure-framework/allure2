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
package io.qameta.allure.xunitxml;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

    /**
     * Verifies an xUnit XML test case is converted into an Allure result.
     * The test checks parsed name, history identifier, and passed status.
     */
    @Description
    @Test
    void shouldCreateTest() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .hasSize(1)
                .extracting(TestResult::getName, TestResult::getHistoryId, TestResult::getStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("passedTest", "Some test", Status.PASSED)
                );
    }

    /**
     * Verifies xUnit execution duration is preserved in the Allure result.
     * The test checks the parsed duration field for a fixture with a known runtime.
     */
    @Description
    @Test
    void shouldSetTime() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .hasSize(1)
                .extracting(TestResult::getTime)
                .extracting(Time::getDuration)
                .containsExactlyInAnyOrder(44L);
    }

    /**
     * Verifies suite, package, class, and format labels are derived from xUnit data.
     * The test checks the exact labels emitted for a simple passed fixture.
     */
    @Description
    @Test
    void shouldSetLabels() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
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

    /**
     * Verifies the xUnit full name is mapped into the Allure result.
     * The test checks the parsed full name for a representative fixture.
     */
    @Description
    @Test
    void shouldSetFullName() throws Exception {
        process(
                "xunitdata/passed-test.xml",
                "passed-test.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .hasSize(1)
                .extracting(TestResult::getFullName)
                .containsExactlyInAnyOrder(
                        "Some test"
                );
    }

    /**
     * Verifies an xUnit framework attribute is emitted as an Allure framework label.
     * The test checks the focused framework label value parsed from the fixture.
     */
    @Description
    @Test
    void shouldSetFramework() throws Exception {
        process(
                "xunitdata/framework-test.xml",
                "passed-test.xml"
        );

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
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

    /**
     * Verifies xUnit status details are parsed for failed and passed cases.
     * The test checks message and trace fields for each fixture variant.
     */
    @Description
    @ParameterizedTest
    @MethodSource("data")
    void shouldSetStatusDetails(final String resource,
                                final String fileName,
                                final String message,
                                final String trace) throws Exception {
        process(resource, fileName);

        final List<TestResult> results = captureTestResults(1);

        assertThat(results)
                .hasSize(1)
                .extracting(TestResult::getStatusMessage, TestResult::getStatusTrace)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(message, trace)
                );
    }

    /**
     * Verifies external XML entities are not resolved while parsing xUnit results.
     * The test checks parsed status details do not leak content from a local secret file.
     */
    @Description
    @Test
    void cveEntityReadTest(@TempDir final Path tmp) throws IOException {
        final Path secretFile = tmp.resolve("secretfile.ini");
        Files.writeString(secretFile,
                "[owner]\n"
                + "name = John Doe\n"
                + "organization = Example Org.\n",
                StandardCharsets.UTF_8
        );

        final String maliciousXml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE foo [\n"
                + "  <!ENTITY xxe SYSTEM \"" + secretFile.toUri() + "\">\n"
                + "]>\n"
                + "<assemblies>\n"
                + "  <assembly test-framework=\"xunit\">\n"
                + "    <collection>\n"
                + "      <test name=\"Exploit Test\" method=\"testMethod\" type=\"TestClass\" result=\"Fail\">\n"
                + "        <failure>\n"
                + "          <message>&xxe;</message>\n"
                + "          <stack-trace>Trace with &xxe;</stack-trace>\n"
                + "        </failure>\n"
                + "      </test>\n"
                + "    </collection>\n"
                + "  </assembly>\n"
                + "</assemblies>";
        writeTextFile(resultsDirectory.resolve("bad-test.xml"), "bad-test.xml", maliciousXml);

        readResults(resultsDirectory);

        final List<TestResult> results = captureTestResults(1);

        final TestResult testResult = results.get(0);
        assertThat(testResult.getStatusMessage()).doesNotContain("John Doe");
        assertThat(testResult.getStatusTrace()).doesNotContain("Example Org");
    }

    private void process(final String... strings) throws IOException {
        Allure.step("Parse xUnit XML results", () -> {
            final Iterator<String> iterator = Arrays.asList(strings).iterator();
            while (iterator.hasNext()) {
                final String first = iterator.next();
                final String second = iterator.next();
                copyFile(resultsDirectory, first, second);
            }
            readResults(resultsDirectory);
        });
    }

    private void readResults(final Path directory) {
        Allure.step("Read xUnit XML results", () -> {
            final XunitXmlPlugin reader = new XunitXmlPlugin();
            reader.readResults(configuration, visitor, directory);
        });
    }

    private void copyFile(final Path dir, final String resourceName, final String fileName) throws IOException {
        Allure.step("Copy fixture resource " + resourceName + " as " + fileName, () -> {
            final byte[] content;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                content = Objects.requireNonNull(is).readAllBytes();
            }
            final Path destination = dir.resolve(fileName);
            Files.createDirectories(destination.getParent());
            Files.write(destination, content);
            Allure.addAttachment(fileName, "application/xml", new String(content, StandardCharsets.UTF_8), ".xml");
        });
    }

    private void writeTextFile(final Path path, final String fileName, final String content) throws IOException {
        Allure.step("Write text fixture " + fileName, () -> {
            Files.writeString(path, content, StandardCharsets.UTF_8);
            Allure.addAttachment(fileName, "application/xml", content, ".xml");
        });
    }

    private List<TestResult> captureTestResults(final int expectedCount) {
        return Allure.step("Capture parsed xUnit test results", () -> {
            final ArgumentCaptor<TestResult> captor = ArgumentCaptor.captor();
            verify(visitor, times(expectedCount)).visitTestResult(captor.capture());
            final List<TestResult> results = captor.getAllValues();
            Allure.addAttachment("parsed-test-results.txt", "text/plain", describeTestResults(results));
            return results;
        });
    }

    private String describeTestResults(final List<TestResult> results) {
        final StringBuilder builder = new StringBuilder();
        builder.append("results=").append(results.size()).append(System.lineSeparator());
        results.forEach(result -> builder
                .append(System.lineSeparator())
                .append("name=").append(result.getName()).append(System.lineSeparator())
                .append("fullName=").append(result.getFullName()).append(System.lineSeparator())
                .append("historyId=").append(result.getHistoryId()).append(System.lineSeparator())
                .append("status=").append(result.getStatus()).append(System.lineSeparator())
                .append("duration=")
                .append(result.getTime() == null ? null : result.getTime().getDuration())
                .append(System.lineSeparator())
                .append("statusMessage=").append(result.getStatusMessage()).append(System.lineSeparator())
                .append("statusTrace=").append(result.getStatusTrace()).append(System.lineSeparator())
                .append("labels=").append(describeLabels(result))
                .append(System.lineSeparator())
        );
        return builder.toString();
    }

    private String describeLabels(final TestResult result) {
        return result.getLabels().stream()
                .map(label -> label.getName() + "=" + label.getValue())
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
