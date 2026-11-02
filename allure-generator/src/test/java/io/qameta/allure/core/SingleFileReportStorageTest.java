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

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.ReportStorage;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SingleFileReportStorageTest {

    private static final JsonMapper MAPPER = new JsonMapper();

    private static final Map<String, String> PAYLOADS = Map.of(
            "stats.json", "{\"passed\":1}",
            "metrics.txt", "passed=1\n",
            "copy.txt", "build=42; ✓\n"
    );

    /**
     * The addData methods embed JSON, byte arrays, and copied files regardless of
     * the output path. History and export prefixes must not override the API choice.
     */
    @Description
    @ParameterizedTest(name = "Embeds report data under {0}")
    @ValueSource(strings = {"data", "widgets", "history", "export", "custom"})
    void shouldEmbedReportDataRegardlessOfPath(final String directory, @TempDir final Path temp)
            throws Exception {
        final Path output = temp.resolve("report");
        final SingleFileReportStorage storage = new SingleFileReportStorage(output);
        final Path source = createSource(temp);
        final Map<String, String> expected = expectedFiles(directory);
        attachJson("Expected generated files", expected);

        Allure.step("Store JSON, a byte array, and a copied file through the original API", () -> {
            storage.addDataJson(directory + "/stats.json", Map.of("passed", 1));
            storage.addDataBinary(directory + "/metrics.txt", PAYLOADS.get("metrics.txt").getBytes(StandardCharsets.UTF_8));
            storage.addDataFile(directory + "/copy.txt", source);
        });

        Allure.step("Verify all report content is embedded and no files are written", () -> {
            final Map<String, String> embedded = readEmbedded(storage);
            final Map<String, String> external = readFiles(output);
            assertThat(embedded).containsExactlyInAnyOrderEntriesOf(expected);
            assertThat(external).isEmpty();
        });
    }

    /**
     * Additional JSON, binary data, and copied files are written to arbitrary paths
     * beside either report mode. The directory storage's default methods preserve
     * compatibility, while single-file storage keeps all three out of its payload.
     */
    @Description
    @ParameterizedTest(name = "Writes additional files with singleFile={0} under {1}")
    @CsvSource({"false, ''", "true, ''", "true, data/", "true, widgets/", "true, history/", "true, export/"})
    void shouldWriteExplicitAdditionalFiles(final boolean singleFile, final String prefix,
                                            @TempDir final Path temp)
            throws Exception {
        final Path output = temp.resolve("report");
        final ReportStorage storage = singleFile
                ? new SingleFileReportStorage(output)
                : new FileSystemReportStorage(output);
        final Path source = createSource(temp);
        final Map<String, String> expected = PAYLOADS.entrySet().stream().collect(
                Collectors.toMap(
                        entry -> prefix + entry.getKey(), Map.Entry::getValue
                )
        );
        attachJson("Expected additional files", expected);

        Allure.step("Store JSON, a byte array, and a copied file through the additional-file API", () -> {
            storage.addAdditionalJson(prefix + "stats.json", Map.of("passed", 1));
            storage.addAdditionalBinary(prefix + "metrics.txt", PAYLOADS.get("metrics.txt").getBytes(StandardCharsets.UTF_8));
            storage.addAdditionalFile(prefix + "copy.txt", source);
        });

        Allure.step("Verify every additional file is written intact and excluded from embedding", () -> {
            assertThat(readFiles(output)).containsExactlyInAnyOrderEntriesOf(expected);
            if (singleFile) {
                assertThat(readEmbedded((SingleFileReportStorage) storage)).isEmpty();
            }
        });
    }

    private static Path createSource(final Path temp) throws Exception {
        attachJson("Input payloads", PAYLOADS);
        return Files.writeString(temp.resolve("source.txt"), PAYLOADS.get("copy.txt"));
    }

    private static Map<String, String> expectedFiles(final String directory) {
        return PAYLOADS.entrySet().stream().collect(
                Collectors.toMap(
                        entry -> directory + "/" + entry.getKey(), Map.Entry::getValue
                )
        );
    }

    private static Map<String, String> readEmbedded(final SingleFileReportStorage storage) throws Exception {
        final Map<String, String> result = new TreeMap<>();
        storage.getReportDataFiles().forEach(
                (name, content) -> result.put(
                        name, new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8)
                )
        );
        attachJson("Actual embedded files", result);
        return result;
    }

    private static Map<String, String> readFiles(final Path output) throws Exception {
        final Map<String, String> result = new TreeMap<>();
        if (Files.exists(output)) {
            try (Stream<Path> files = Files.walk(output)) {
                for (Path file : files.filter(Files::isRegularFile).collect(Collectors.toList())) {
                    result.put(
                            output.relativize(file).toString().replace(File.separatorChar, '/'),
                            Files.readString(file)
                    );
                }
            }
        }
        attachJson("Actual files on disk", result);
        return result;
    }

    private static void attachJson(final String name, final Map<String, String> data) throws Exception {
        Allure.addAttachment(
                name, "application/json", MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data),
                ".json"
        );
    }
}
