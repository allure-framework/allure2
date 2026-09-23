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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.summary.SummaryExportPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReportSummaryTest {

    private static final JsonMapper MAPPER = new JsonMapper();

    /**
     * Exports the Allure 3 summary contract from final results across input directories.
     * A failed retry must not inflate counts or extend the duration of the report.
     */
    @Description
    @ParameterizedTest(name = "Exports final results with singleFile={0}")
    @ValueSource(booleans = {false, true})
    void shouldWriteSummaryForFinalResults(final boolean singleFile, @TempDir final Path temp) throws Exception {
        final Path firstResults = Files.createDirectories(temp.resolve("first-results"));
        final Path secondResults = Files.createDirectories(temp.resolve("second-results"));
        Allure.step("Prepare a failed checkout attempt, its passing retry, and a broken payment test", () -> {
            writeResult(firstResults, "checkout-first", "checkout", "failed", 1000, 1500);
            writeResult(secondResults, "checkout-retry", "checkout", "passed", 2000, 5000);
            writeResult(secondResults, "payment", "payment", "broken", 2500, 4000);
        });
        final Configuration configuration = ConfigurationBuilder.bundled()
                .withReportName("Build #42")
                .build();
        final Path output = temp.resolve("report");

        generateReport(configuration, output, List.of(firstResults, secondResults), singleFile);

        final JsonNode expected = MAPPER.readTree("""
                {
                  "name": "Build #42",
                  "stats": {"failed": 0, "broken": 1, "skipped": 0, "passed": 1, "unknown": 0, "total": 2},
                  "status": "broken",
                  "duration": 3000,
                  "createdAt": 5000,
                  "plugin": "Allure2",
                  "meta": {"reportId": "%s", "singleFile": %s}
                }
                """.formatted(configuration.getUuid(), singleFile));
        assertSummary(output, expected);
        Allure.step("Verify the widget remains available in the report's existing storage mode", () -> {
            assertThat(Files.exists(output.resolve("widgets/summary.json"))).isEqualTo(!singleFile);
        });
    }

    /**
     * Empty reports still provide a machine-readable summary with zero counts and the
     * same passed status, zero duration, and zero timestamp as Allure 3.
     */
    @Description
    @ParameterizedTest(name = "Exports empty report with singleFile={0}")
    @ValueSource(booleans = {false, true})
    void shouldWriteEmptySummary(final boolean singleFile, @TempDir final Path temp) throws Exception {
        final Configuration configuration = ConfigurationBuilder.bundled().build();
        final Path output = temp.resolve("report");

        generateReport(configuration, output, List.of(), singleFile);

        final JsonNode expected = MAPPER.readTree("""
                {
                  "name": "Allure Report",
                  "stats": {"failed": 0, "broken": 0, "skipped": 0, "passed": 0, "unknown": 0, "total": 0},
                  "status": "passed",
                  "duration": 0,
                  "createdAt": 0,
                  "plugin": "Allure2",
                  "meta": {"reportId": "%s", "singleFile": %s}
                }
                """.formatted(configuration.getUuid(), singleFile));
        assertSummary(output, expected);
    }

    /**
     * A configuration without the summary export plugin generates its HTML but
     * does not emit a CI summary as an implicit generator side effect.
     */
    @Description
    @ParameterizedTest(name = "Omits unconfigured summary with singleFile={0}")
    @ValueSource(booleans = {false, true})
    void shouldNotWriteSummaryWithoutPlugin(final boolean singleFile, @TempDir final Path temp) {
        final Configuration configuration = ConfigurationBuilder.empty().build();
        final Path output = temp.resolve("report");

        generateReport(configuration, output, List.of(), singleFile);

        Allure.step("Verify the report exists without an unconfigured summary export", () -> {
            assertThat(output.resolve("index.html")).isRegularFile();
            assertThat(output.resolve("summary.json")).doesNotExist();
        });
    }

    /**
     * CI summaries, history, and integration exports accompany a portable HTML file.
     * Widgets and CSV downloads stay embedded, so viewing requires only index.html.
     */
    @Description
    @Test
    void shouldKeepSingleFileReportSelfContained(@TempDir final Path temp) throws Exception {
        final Path results = Files.createDirectories(temp.resolve("results"));
        Allure.step("Prepare one passing checkout test", () -> {
            writeResult(results, "checkout", "checkout", "passed", 1000, 2000);
        });
        final Path output = temp.resolve("report");
        generateReport(ConfigurationBuilder.bundled().build(), output, List.of(results), true);

        try (Stream<Path> files = Files.walk(output)) {
            assertThat(
                    files.filter(Files::isRegularFile).map(output::relativize)
                            .map(path -> path.toString().replace(File.separatorChar, '/'))
            )
                    .containsExactlyInAnyOrder(
                            "index.html", "summary.json",
                            "history/history.json", "history/history-trend.json", "history/duration-trend.json",
                            "history/retry-trend.json", "history/categories-trend.json",
                            "export/mail.html", "export/prometheusData.txt", "export/influxDbData.txt"
                    );
        }
        Allure.step("Read embedded widget data from the standalone HTML", () -> {
            final Map<String, String> reportData = readEmbeddedData(output);
            assertThat(reportData).containsKeys(
                    "widgets/summary.json", "widgets/history-trend.json", "data/suites.csv", "data/categories.csv"
            );
            assertThat(reportData.keySet()).noneMatch(
                    name -> name.startsWith("history/")
                            || name.startsWith("export/") || name.equals("summary.json")
            );
            assertThat(reportData.get("data/suites.csv")).contains("checkout", "passed");
            Allure.addAttachment("Embedded data/suites.csv", "text/csv", reportData.get("data/suites.csv"), ".csv");
            final JsonNode embedded = MAPPER.readTree(reportData.get("widgets/summary.json"));
            attachJson("Embedded widgets/summary.json", embedded);
            assertThat(embedded.path("statistic")).isEqualTo(MAPPER.readTree("""
                    {"failed": 0, "broken": 0, "skipped": 0, "passed": 1, "unknown": 0, "total": 1}
                    """));
        });
    }

    /**
     * Reuses a single-file report's history in the next build. Both the standalone
     * history files and the embedded trend widget must preserve the previous build.
     */
    @Description
    @Test
    void shouldCarryHistoryBetweenSingleFileReports(@TempDir final Path temp) throws Exception {
        final Path firstInput = Files.createDirectories(temp.resolve("first-results"));
        final Path secondInput = Files.createDirectories(temp.resolve("second-results"));
        Allure.step("Prepare two builds of the same test, first passed and then failed", () -> {
            writeResult(firstInput, "first", "checkout", "passed", 1000, 2000);
            writeResult(secondInput, "second", "checkout", "failed", 3000, 4500);
            writeExecutor(firstInput, 1);
            writeExecutor(secondInput, 2);
        });
        final Path firstReport = temp.resolve("first-report");
        final Path secondReport = temp.resolve("second-report");
        generateReport(ConfigurationBuilder.bundled().build(), firstReport, List.of(firstInput), true);
        Allure.step("Copy the first report's exported history into the second build's results", () -> {
            final Path previousHistory = firstReport.resolve("history");
            assertThat(previousHistory).isDirectory();
            final Path nextHistory = Files.createDirectories(secondInput.resolve("history"));
            try (Stream<Path> files = Files.list(previousHistory)) {
                for (Path source : files.collect(Collectors.toList())) {
                    Files.copy(source, nextHistory.resolve(source.getFileName()));
                    attachJson("Previous " + source.getFileName(), MAPPER.readTree(source.toFile()));
                }
            }
        });

        generateReport(ConfigurationBuilder.bundled().build(), secondReport, List.of(secondInput), true);

        Allure.step("Verify both builds survive in the exported per-test history and embedded trends", () -> {
            final JsonNode history = MAPPER.readTree(secondReport.resolve("history/history.json").toFile());
            attachJson("Second report history/history.json", history);
            assertThat(history.size()).isEqualTo(1);
            final JsonNode testHistory = history.elements().next();
            assertThat(testHistory.path("statistic")).isEqualTo(MAPPER.readTree("""
                    {"failed": 1, "broken": 0, "skipped": 0, "passed": 1, "unknown": 0, "total": 2}
                    """));
            assertThat(testHistory.path("items").findValuesAsText("status")).containsExactly("failed", "passed");
            final JsonNode trend = MAPPER.readTree(secondReport.resolve("history/history-trend.json").toFile());
            attachJson("Second report history/history-trend.json", trend);
            assertThat(trend.findValuesAsText("buildOrder")).containsExactly("2", "1");
            assertThat(trend.get(0).path("data").path("failed").intValue()).isEqualTo(1);
            assertThat(trend.get(1).path("data").path("passed").intValue()).isEqualTo(1);
            final Map<String, String> embedded = readEmbeddedData(secondReport);
            final JsonNode widget = MAPPER.readTree(embedded.get("widgets/history-trend.json"));
            attachJson("Embedded widgets/history-trend.json", widget);
            assertThat(widget).isEqualTo(trend);
        });
    }

    /**
     * Matches Allure 3 timing semantics for duration-only results, incomplete timestamp
     * pairs, reversed timestamps, and results without timing information.
     */
    @Description
    @ParameterizedTest(name = "Exports duration for {0}")
    @MethodSource("summaryTimes")
    void shouldUseAllure3TimingSemantics(final String scenario, final Time firstTime, final Time secondTime,
                                         final long expectedDuration, final long expectedCreatedAt,
                                         @TempDir final Path temp)
            throws Exception {
        final List<TestResult> results = List.of(
                new TestResult().setUid("first").setName("first").setStatus(Status.PASSED).setTime(firstTime),
                new TestResult().setUid("second").setName("second").setStatus(Status.PASSED).setTime(secondTime)
        );
        attachJson("Processed results", MAPPER.valueToTree(results));
        final Reader reader = (configuration, visitor, directory) -> results.forEach(visitor::visitTestResult);
        final Configuration configuration = ConfigurationBuilder.empty()
                .withExtensions(List.of(reader, new SummaryExportPlugin()))
                .build();
        final Path input = Files.createDirectories(temp.resolve("results"));
        final Path output = temp.resolve("report");

        generateReport(configuration, output, List.of(input), true);

        final JsonNode summary = readSummary(output);
        assertThat(summary.path("duration").longValue()).as("Duration in milliseconds").isEqualTo(expectedDuration);
        assertThat(summary.path("createdAt").longValue()).as("Latest finish timestamp").isEqualTo(expectedCreatedAt);
    }

    /**
     * The summary export plugin works independently of the overview widget plugin
     * and emits the CI sidecar in either report mode when explicitly configured.
     */
    @Description
    @ParameterizedTest(name = "Exports summary without widgets with singleFile={0}")
    @ValueSource(booleans = {false, true})
    void shouldConfigureSummaryExportIndependently(final boolean singleFile, @TempDir final Path temp) throws Exception {
        final Configuration configuration = ConfigurationBuilder.empty()
                .withExtensions(List.of(new SummaryExportPlugin()))
                .build();
        final Path output = temp.resolve("report");

        generateReport(configuration, output, List.of(), singleFile);

        Allure.step("Verify the independent plugin writes only the CI summary", () -> {
            final JsonNode summary = readSummary(output);
            assertThat(summary.path("stats").path("total").longValue()).isZero();
            assertThat(summary.path("status").textValue()).isEqualTo("passed");
            assertThat(summary.path("meta").path("singleFile").booleanValue()).isEqualTo(singleFile);
            assertThat(output.resolve("widgets/summary.json")).doesNotExist();
            if (singleFile) {
                assertThat(readEmbeddedData(output)).isEmpty();
            }
        });
    }

    static Stream<Arguments> summaryTimes() {
        return Stream.of(
                Arguments.of("duration-only results", Time.create(200L), Time.create(300L), 500L, 0L),
                Arguments.of(
                        "a stop without a start", new Time().setStop(9000L), Time.create(1000L, 2000L),
                        1000L, 9000L
                ),
                Arguments.of(
                        "a start without a stop", new Time().setStart(500L), Time.create(1000L, 2000L),
                        1000L, 2000L
                ),
                Arguments.of("reversed timestamps", Time.create(1000L, 500L), new Time(), 0L, 500L),
                Arguments.of("missing timing information", null, Time.create(200L), 200L, 0L)
        );
    }

    private static void writeResult(final Path directory, final String uuid, final String name,
                                    final String status, final long start, final long stop)
            throws Exception {
        final String content = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(
                Map.of(
                        "uuid", uuid, "name", name, "fullName", name, "status", status, "start", start, "stop", stop
                )
        );
        final String filename = uuid + "-result.json";
        Files.writeString(directory.resolve(filename), content);
        Allure.addAttachment(filename, "application/json", content, ".json");
    }

    private static void generateReport(final Configuration configuration, final Path output,
                                       final List<Path> results, final boolean singleFile) {
        Allure.step("Generate report with singleFile=" + singleFile, () -> {
            final ReportGenerator generator = new ReportGenerator(configuration);
            if (singleFile) {
                generator.generateSingleFile(output, results);
            } else {
                generator.generate(output, results);
            }
            try (Stream<Path> files = Files.walk(output)) {
                Allure.addAttachment(
                        "Generated report files", "text/plain", files
                                .filter(Files::isRegularFile)
                                .map(output::relativize)
                                .map(Path::toString)
                                .sorted()
                                .collect(Collectors.joining("\n")),
                        ".txt"
                );
            }
        });
    }

    private static void writeExecutor(final Path input, final int buildOrder) throws Exception {
        final JsonNode executor = MAPPER.valueToTree(
                Map.of(
                        "name", "Jenkins", "type", "jenkins", "buildOrder", buildOrder,
                        "reportName", "Build #" + buildOrder,
                        "reportUrl", "https://ci.example/builds/" + buildOrder + "/index.html"
                )
        );
        MAPPER.writeValue(input.resolve("executor.json").toFile(), executor);
        attachJson("Build #" + buildOrder + " executor.json", executor);
    }

    private static Map<String, String> readEmbeddedData(final Path output) throws Exception {
        final Matcher entries = Pattern.compile("d\\('([^']+)','([^']*)'\\)")
                .matcher(Files.readString(output.resolve("index.html")));
        final Map<String, String> data = new TreeMap<>();
        while (entries.find()) {
            data.put(
                    entries.group(1), new String(
                            Base64.getDecoder().decode(entries.group(2)),
                            StandardCharsets.UTF_8
                    )
            );
        }
        attachJson("Embedded report data", MAPPER.valueToTree(data));
        return data;
    }

    private static void assertSummary(final Path output, final JsonNode expected) {
        Allure.step("Verify summary.json matches the Allure 3 contract", () -> {
            attachJson("Expected summary.json", expected);
            assertThat(readSummary(output)).isEqualTo(expected);
        });
    }

    private static JsonNode readSummary(final Path output) throws Exception {
        final Path summaryFile = output.resolve("summary.json");
        assertThat(summaryFile).isRegularFile();
        final JsonNode actual = MAPPER.readTree(summaryFile.toFile());
        attachJson("summary.json", actual);
        return actual;
    }

    private static void attachJson(final String name, final JsonNode data) {
        Allure.addAttachment(name, "application/json", data.toPrettyString(), ".json");
    }
}
