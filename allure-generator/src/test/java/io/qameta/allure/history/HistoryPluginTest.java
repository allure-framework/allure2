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
package io.qameta.allure.history;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.Status.FAILED;
import static io.qameta.allure.entity.Status.PASSED;
import static io.qameta.allure.testdata.TestData.createLaunchResults;
import static io.qameta.allure.testdata.TestData.randomTestResult;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class HistoryPluginTest {

    private static final String HISTORY_BLOCK_NAME = "history";
    private static final String PARAMETERS_HASH = "parameters";

    /**
     * Verifies detecting the new failed mark for history aggregation.
     */
    @Description
    @Test
    void shouldHasNewFailedMark() {
        String testCaseHash = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                testCaseHash,
                createHistoryItem(PASSED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(FAILED, testCaseHash, 100, 101);
        getHistoryData(extra, testResult);
        assertThat(testResult.isNewFailed()).isTrue();
        assertThat(testResult.isFlaky()).isFalse();
        assertThat(testResult.isNewPassed()).isFalse();
        assertThat(testResult.isNewBroken()).isFalse();
    }

    /**
     * Verifies detecting the new broken mark for history aggregation.
     */
    @Description
    @Test
    void shouldHasNewBrokenMark() {
        String testCaseHash = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                testCaseHash,
                createHistoryItem(PASSED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(Status.BROKEN, testCaseHash, 100, 101);
        getHistoryData(extra, testResult);
        assertThat(testResult.isNewFailed()).isFalse();
        assertThat(testResult.isFlaky()).isFalse();
        assertThat(testResult.isNewPassed()).isFalse();
        assertThat(testResult.isNewBroken()).isTrue();
    }

    /**
     * Verifies detecting the flaky mark for history aggregation.
     */
    @Description
    @Test
    void shouldHasFlakyMark() {
        String testCaseHash = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                testCaseHash,
                createHistoryItem(PASSED, 3, 4),
                createHistoryItem(FAILED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(FAILED, testCaseHash, 100, 101);
        getHistoryData(extra, testResult);
        assertThat(testResult.isNewFailed()).isTrue();
        assertThat(testResult.isFlaky()).isTrue();
        assertThat(testResult.isNewPassed()).isFalse();
        assertThat(testResult.isNewBroken()).isFalse();
    }

    /**
     * Verifies detecting the new passed mark for history aggregation.
     */
    @Description
    @Test
    void shouldHasNewPassedMark() {
        String testCaseHash = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                testCaseHash,
                createHistoryItem(FAILED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(Status.PASSED, testCaseHash, 100, 101);
        getHistoryData(extra, testResult);
        assertThat(testResult.isNewFailed()).isFalse();
        assertThat(testResult.isFlaky()).isFalse();
        assertThat(testResult.isNewPassed()).isTrue();
        assertThat(testResult.isNewBroken()).isFalse();
    }

    /**
     * Verifies reducing history data across multiple launches for history aggregation.
     */
    @Description
    @Test
    void shouldReduceHistoryResults() {
        String testCaseHash1 = UUID.randomUUID().toString();
        String testCaseHash2 = UUID.randomUUID().toString();
        final Map<String, Object> extra1 = new HashMap<>();
        final Map<String, Object> extra2 = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = new HashMap<>();
        historyDataMap.put(retryHash(testCaseHash1), new HistoryData().setItems(singletonList(createHistoryItem(PASSED, 1, 2))));
        historyDataMap.put(retryHash(testCaseHash2), new HistoryData().setItems(singletonList(createHistoryItem(PASSED, 2, 3))));

        extra1.put(HISTORY_BLOCK_NAME, historyDataMap);
        extra2.put(HISTORY_BLOCK_NAME, copyHistoryData(historyDataMap));

        Map<String, HistoryData> data = Allure.step(
                "Reduce history entries across two launches",
                () -> new HistoryPlugin().getData(
                        asList(
                                createLaunchResults(extra1, createTestResult(PASSED, testCaseHash1, 3, 4)),
                                createLaunchResults(extra2, createTestResult(PASSED, testCaseHash2, 5, 6))
                        )
                )
        );

        assertThat(data).containsKeys(retryHash(testCaseHash1), retryHash(testCaseHash2));
        assertThat(data.get(retryHash(testCaseHash1)).getItems()).hasSize(2);
        assertThat(data.get(retryHash(testCaseHash2)).getItems()).hasSize(2);
    }

    /**
     * Verifies history falls back to the adapter-provided key when the canonical retry hash is absent.
     */
    @Description
    @Test
    void shouldFallbackToLegacyHistory() {
        final String testCaseHash = UUID.randomUUID().toString();
        final String legacyHistoryId = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = new HashMap<>();
        historyDataMap.put(
                legacyHistoryId,
                new HistoryData()
                        .setItems(singletonList(createHistoryItem(PASSED, 1, 2)))
        );
        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        final TestResult testResult = createTestResult(FAILED, testCaseHash, 100, 101)
                .setLegacyHistoryId(legacyHistoryId);

        final Map<String, HistoryData> data = getHistoryData(extra, testResult);

        assertThat(data).containsKey(retryHash(testCaseHash));
        assertThat(data.get(retryHash(testCaseHash)).getItems())
                .extracting(HistoryItem::getStatus)
                .containsExactly(FAILED, PASSED);
        assertThat(testResult.<HistoryData>getExtraBlock(HISTORY_BLOCK_NAME).getItems())
                .extracting(HistoryItem::getStatus)
                .containsExactly(PASSED);
        assertThat(testResult.isNewFailed()).isTrue();
    }

    private Map<String, HistoryData> copyHistoryData(Map<String, HistoryData> historyDataMap) {
        return historyDataMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new HistoryData().setItems(e.getValue().getItems())));
    }

    private TestResult createTestResult(Status status, String testCaseHash, long start, long stop) {
        return randomTestResult()
                .setTestCaseHash(testCaseHash)
                .setParametersHash(PARAMETERS_HASH)
                .setStatus(status)
                .setTime(new Time().setStart(start).setStop(stop));
    }

    private Map<String, HistoryData> createHistoryDataMap(String testCaseHash, HistoryItem... historyItems) {
        Map<String, HistoryData> historyDataMap = new HashMap<>();
        historyDataMap.put(retryHash(testCaseHash), new HistoryData().setItems(asList(historyItems)));
        return historyDataMap;
    }

    private String retryHash(final String testCaseHash) {
        return testCaseHash + "." + PARAMETERS_HASH;
    }

    private HistoryItem createHistoryItem(Status status, long start, long stop) {
        return new HistoryItem()
                .setStatus(status)
                .setTime(new Time().setStart(start).setStop(stop));
    }

    private Map<String, HistoryData> getHistoryData(final Map<String, Object> extra, final TestResult testResult) {
        return Allure.step(
                "Calculate history marks for result " + testResult.getName(),
                () -> new HistoryPlugin().getData(singletonList(createLaunchResults(extra, testResult)))
        );
    }

}
