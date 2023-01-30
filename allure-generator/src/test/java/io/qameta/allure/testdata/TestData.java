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
package io.qameta.allure.testdata;

import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.LaunchResults;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * @since 2.0
 */
public final class TestData {

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
        return new DefaultLaunchResults(Arrays.stream(input).collect(Collectors.toSet()), null, extra);
    }

    public static void unpackFile(final String name, final Path output) {
        try (InputStream is = TestData.class.getClassLoader().getResourceAsStream(name)) {
            Files.copy(Objects.requireNonNull(is), output);
        } catch (IOException e) {
            throw new RuntimeException("Could not read resource " + name, e);
        }
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
}
