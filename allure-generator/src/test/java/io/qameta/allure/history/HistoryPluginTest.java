/*
 *  Copyright 2019 Qameta Software OÜ
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.qameta.allure.testdata.TestData.createLaunchResults;
import static io.qameta.allure.testdata.TestData.randomTestResult;
import static org.assertj.core.api.Assertions.assertThat;

class HistoryPluginTest {

    private static final String HISTORY_BLOCK_NAME = "history";

    @Test
    void shouldHasNewFailedMark() {
        String historyId = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                historyId,
                createHistoryItem(Status.PASSED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(Status.FAILED, historyId, 100, 101);
        new HistoryPlugin().getData(Collections.singletonList(
                createLaunchResults(extra, testResult)
        ));
        assertThat(testResult.isNewFailed()).isTrue();
        assertThat(testResult.isFlaky()).isFalse();
    }

    @Test
    void shouldHasFlakyMark() {
        String historyId = UUID.randomUUID().toString();
        final Map<String, Object> extra = new HashMap<>();
        final Map<String, HistoryData> historyDataMap = createHistoryDataMap(
                historyId,
                createHistoryItem(Status.PASSED, 3, 4),
                createHistoryItem(Status.FAILED, 1, 2)
        );

        extra.put(HISTORY_BLOCK_NAME, historyDataMap);
        TestResult testResult = createTestResult(Status.FAILED, historyId, 100, 101);
        new HistoryPlugin().getData(Collections.singletonList(
                createLaunchResults(extra, testResult)
        ));
        assertThat(testResult.isNewFailed()).isTrue();
        assertThat(testResult.isFlaky()).isTrue();
    }

    private TestResult createTestResult(Status status, String historyId, long start, long stop) {
        return randomTestResult()
                .setHistoryId(historyId)
                .setStatus(status)
                .setTime(new Time().setStart(start).setStop(stop));
    }

    private Map<String, HistoryData> createHistoryDataMap(String historyId, HistoryItem... historyItems) {
        Map<String, HistoryData> historyDataMap = new HashMap<>();
        historyDataMap.put(historyId, new HistoryData().setItems(Arrays.asList(historyItems)));
        return historyDataMap;
    }

    private HistoryItem createHistoryItem(Status status, long start, long stop) {
        return new HistoryItem()
                .setStatus(status)
                .setTime(new Time().setStart(start).setStop(stop));
    }

}
