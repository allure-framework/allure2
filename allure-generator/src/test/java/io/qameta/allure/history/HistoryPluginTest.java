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
package io.qameta.allure.history;

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

    @Test
    void shouldHasNewFailedMark() {
        String historyId = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                historyId,
                createHistoryItem(PASSED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(FAILED, historyId, 100, 101);
        new HistoryPlugin().getData(singletonList(
                createLaunchResults(extra, testResult)
        ));
        assertThat(testResult.isNewFailed()).isTrue();
        assertThat(testResult.isFlaky()).isFalse();
        assertThat(testResult.isNewPassed()).isFalse();
        assertThat(testResult.isNewBroken()).isFalse();
    }

    @Test
    void shouldHasNewBrokenMark() {
        String historyId = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                historyId,
                createHistoryItem(PASSED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(Status.BROKEN, historyId, 100, 101);
        new HistoryPlugin().getData(singletonList(
                createLaunchResults(extra, testResult)
        ));
        assertThat(testResult.isNewFailed()).isFalse();
        assertThat(testResult.isFlaky()).isFalse();
        assertThat(testResult.isNewPassed()).isFalse();
        assertThat(testResult.isNewBroken()).isTrue();
    }

    @Test
    void shouldHasFlakyMark() {
        String historyId = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                historyId,
                createHistoryItem(PASSED, 3, 4),
                createHistoryItem(FAILED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(FAILED, historyId, 100, 101);
        new HistoryPlugin().getData(singletonList(
                createLaunchResults(extra, testResult)
        ));
        assertThat(testResult.isNewFailed()).isTrue();
        assertThat(testResult.isFlaky()).isTrue();
        assertThat(testResult.isNewPassed()).isFalse();
        assertThat(testResult.isNewBroken()).isFalse();
    }

    @Test
    void shouldHasNewPassedMark() {
        String historyId = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
            historyId,
            createHistoryItem(FAILED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(Status.PASSED, historyId, 100, 101);
        new HistoryPlugin().getData(singletonList(
            createLaunchResults(extra, testResult)
        ));
        assertThat(testResult.isNewFailed()).isFalse();
        assertThat(testResult.isFlaky()).isFalse();
        assertThat(testResult.isNewPassed()).isTrue();
        assertThat(testResult.isNewBroken()).isFalse();
    }

    @Test
    void shouldReduceHistoryResults() {
        String historyId1 = UUID.randomUUID().toString();
        String historyId2 = UUID.randomUUID().toString();
        final Map<String, Object> extra1 = new HashMap<>();
        final Map<String, Object> extra2 = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = new HashMap<>();
        historyDataMap.put(historyId1, new HistoryData().setItems(singletonList(createHistoryItem(PASSED, 1, 2))));
        historyDataMap.put(historyId2, new HistoryData().setItems(singletonList(createHistoryItem(PASSED, 2, 3))));

        extra1.put(HISTORY_BLOCK_NAME, historyDataMap);
        extra2.put(HISTORY_BLOCK_NAME, copyHistoryData(historyDataMap));


        Map<String, HistoryData> data = new HistoryPlugin().getData(asList(
                createLaunchResults(extra1, createTestResult(PASSED, historyId1, 3, 4)),
                createLaunchResults(extra2, createTestResult(PASSED, historyId2, 5, 6))
        ));

        assertThat(data).containsKeys(historyId1, historyId2);
        assertThat(data.get(historyId1).getItems()).hasSize(2);
        assertThat(data.get(historyId2).getItems()).hasSize(2);
    }

    private Map<String, HistoryData> copyHistoryData(Map<String, HistoryData> historyDataMap) {
        return historyDataMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new HistoryData().setItems(e.getValue().getItems())));
    }

    private TestResult createTestResult(Status status, String historyId, long start, long stop) {
        return randomTestResult()
                .setHistoryId(historyId)
                .setStatus(status)
                .setTime(new Time().setStart(start).setStop(stop));
    }

    private Map<String, HistoryData> createHistoryDataMap(String historyId, HistoryItem... historyItems) {
        Map<String, HistoryData> historyDataMap = new HashMap<>();
        historyDataMap.put(historyId, new HistoryData().setItems(asList(historyItems)));
        return historyDataMap;
    }

    private HistoryItem createHistoryItem(Status status, long start, long stop) {
        return new HistoryItem()
                .setStatus(status)
                .setTime(new Time().setStart(start).setStop(stop));
    }

}
