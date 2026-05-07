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
package io.qameta.allure.testdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.history.HistoryTrendItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * @since 2.0
 */
public final class TestData {

    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";
    private static final ObjectMapper JSON_MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    private TestData() {
        throw new IllegalStateException("Do not instance");
    }

    public static List<LaunchResults> createSingleLaunchResults(TestResult... input) {
        return createSingleLaunchResults(new HashMap<>(), input);
    }

    public static List<LaunchResults> createSingleLaunchResults(Map<String, Object> extra, TestResult... input) {
        List<LaunchResults> launchResultsList = new ArrayList<>();
        launchResultsList.add(createLaunchResults(extra, input));
        return launchResultsList;
    }

    public static DefaultLaunchResults createLaunchResults(final Map<String, Object> extra, final TestResult... input) {
        return Allure.step(
                String.format(
                        "Create launch results with %d test result(s) and %d extra block(s)",
                        input.length,
                        extra.size()
                ),
                () -> {
                    attachJson("launch-results.json", Arrays.asList(input));
                    attachJson("launch-extra.json", extra);
                    return new DefaultLaunchResults(Arrays.stream(input).collect(Collectors.toSet()), null, extra);
                }
        );
    }

    public static void unpackFile(final String name, final Path output) {
        Allure.step("Copy fixture resource " + name + " as " + output.getFileName(), () -> {
            final byte[] content;
            try (InputStream is = TestData.class.getClassLoader().getResourceAsStream(name)) {
                content = Objects.requireNonNull(is).readAllBytes();
                Files.write(output, content);
            } catch (IOException e) {
                throw new RuntimeException("Could not read resource " + name, e);
            }
            attachFileContent(output.getFileName().toString(), content);
        });
    }

    public static List<String> allure1data() {
        try (InputStream is = TestData.class.getClassLoader().getResourceAsStream("allure1data.txt")) {
            return IOUtils.readLines(Objects.requireNonNull(is), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not read resource allure1data.txt", e);
        }
    }

    public static List<HistoryTrendItem> randomHistoryTrendItems() {
        return Arrays.asList(
                randomHistoryTrendItem(),
                randomHistoryTrendItem(),
                randomHistoryTrendItem()
        );
    }

    public static HistoryTrendItem randomHistoryTrendItem() {
        return new HistoryTrendItem()
                .setStatistic(randomStatistic())
                .setBuildOrder(current().nextLong(100))
                .setReportName(randomString())
                .setReportUrl(randomString());
    }

    public static Statistic randomStatistic() {
        return new Statistic()
                .setFailed(current().nextLong(10))
                .setBroken(current().nextLong(10))
                .setPassed(current().nextLong(10))
                .setSkipped(current().nextLong(10))
                .setUnknown(current().nextLong(10));
    }

    public static TestResult randomTestResult() {
        return new TestResult().setName(randomString());
    }

    public static String randomString() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String toHex(final byte[] bytes) {
        return toHex(bytes, bytes.length);
    }

    public static String toHex(final byte[] bytes, final int limit) {
        final StringBuilder builder = new StringBuilder();
        final int length = Math.min(bytes.length, limit);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            final String hex = Integer.toHexString(Byte.toUnsignedInt(bytes[i]));
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        if (bytes.length > limit) {
            builder.append(" ...");
        }
        return builder.toString();
    }

    public static void attachFileContent(final String fileName, final byte[] content) {
        final String body = formatFileContent(fileName, content);
        Allure.addAttachment(fileName, getContentType(fileName), body, getFileExtension(fileName));
    }

    public static void attachLaunchResults(final String stepName, final LaunchResults results) {
        Allure.step(stepName, () -> {
            final List<TestResult> testResults = sortedResults(results.getAllResults());
            attachJson("launch-results.json", testResults);
            attachJson("launch-attachments.json", describeAttachments(results));

            results.getAttachments().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(
                            entry -> attachFileContent(
                                    entry.getKey().getFileName().toString(),
                                    readAttachment(entry.getKey())
                            )
                    );
        });
    }

    private static List<TestResult> sortedResults(final Set<TestResult> results) {
        return results.stream()
                .sorted((left, right) -> resultSortKey(left).compareTo(resultSortKey(right)))
                .collect(Collectors.toList());
    }

    private static String resultSortKey(final TestResult result) {
        return String.join(
                "|",
                Objects.toString(result.getFullName(), ""),
                Objects.toString(result.getName(), ""),
                Objects.toString(result.getUid(), "")
        );
    }

    private static List<Map<String, Object>> describeAttachments(final LaunchResults results) {
        return results.getAttachments().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(TestData::describeAttachment)
                .collect(Collectors.toList());
    }

    private static Map<String, Object> describeAttachment(final Map.Entry<Path, Attachment> entry) {
        final Attachment attachment = entry.getValue();
        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileName", entry.getKey().getFileName().toString());
        result.put("name", attachment.getName());
        result.put("type", attachment.getType());
        result.put("source", attachment.getSource());
        result.put("size", attachment.getSize());
        return result;
    }

    private static byte[] readAttachment(final Path attachment) {
        try {
            return Files.readAllBytes(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Could not read attachment " + attachment, e);
        }
    }

    private static String formatFileContent(final String fileName, final byte[] content) {
        final String text = new String(content, StandardCharsets.UTF_8);
        if (fileName.endsWith(".json")) {
            return prettifyJson(text);
        }
        return text;
    }

    private static String getContentType(final String fileName) {
        return fileName.endsWith(".json") ? APPLICATION_JSON : TEXT_PLAIN;
    }

    private static String getFileExtension(final String fileName) {
        final int index = fileName.lastIndexOf('.');
        return index < 0 ? ".txt" : fileName.substring(index);
    }

    private static void attachJson(final String fileName, final Object value) {
        Allure.addAttachment(fileName, APPLICATION_JSON, toJson(value), ".json");
    }

    private static String prettifyJson(final String value) {
        try {
            return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(JSON_MAPPER.readTree(value));
        } catch (JsonProcessingException ignored) {
            return value;
        }
    }

    private static String toJson(final Object value) {
        try {
            return JSON_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize launch evidence", e);
        }
    }
}
