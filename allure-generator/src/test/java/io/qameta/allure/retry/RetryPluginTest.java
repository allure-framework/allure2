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
package io.qameta.allure.retry;

import io.qameta.allure.Description;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.qameta.allure.retry.RetryPlugin.RETRY_BLOCK_NAME;
import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * eroshenkoam
 * 19.04.17
 */
class RetryPluginTest {

    private static final String FIRST_RESULT = "first";
    private static final String SECOND_RESULT = "second";
    private static final String LAST_RESULT = "last";
    private static final String PARAMETERS_HASH = "parameters";

    private RetryPlugin retryPlugin = new RetryPlugin();

    /**
     * Verifies merging retries test results for retry aggregation.
     */
    @Description
    @Test
    void shouldMergeRetriesTestResults() {
        String retryHash = UUID.randomUUID().toString();

        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                createTestResult(FIRST_RESULT, retryHash, 1L, 9L),
                createTestResult(SECOND_RESULT, retryHash, 11L, 19L),
                createTestResult(LAST_RESULT, retryHash, 21L, 29L)
        );

        retryPlugin.aggregate(null, launchResultsList, null);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();

        assertThat(results).as("test retries")
                .filteredOn(TestResult::isHidden)
                .extracting(TestResult::getName)
                .containsExactlyInAnyOrder(FIRST_RESULT, SECOND_RESULT);

        TestResult lastResult = results.stream()
                .filter(r -> !r.isHidden())
                .findFirst()
                .orElseGet(null);

        assertThat(Collections.singletonList(lastResult))
                .as("latest test result")
                .extracting(TestResult::getName, TestResult::isHidden, TestResult::isFlaky, TestResult::getRetriesCount)
                .containsExactlyInAnyOrder(
                        tuple(LAST_RESULT, false, false, 2)
                );

        assertThat(results).as("test results with retries block")
                .filteredOn(result -> result.hasExtraBlock(RETRY_BLOCK_NAME))
                .hasSize(1);

        List<RetryItem> retries = lastResult.getExtraBlock(RETRY_BLOCK_NAME);
        assertThat(retries).as("test results retries block")
                .isNotNull()
                .hasSize(2);
    }

    /**
     * Verifies retry aggregation keeps unrelated retry hashes separate.
     */
    @Description
    @Test
    void shouldNotMergeOtherTestResults() {
        String firstRetryHash = UUID.randomUUID().toString();
        String secondRetryHash = UUID.randomUUID().toString();

        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                createTestResult(FIRST_RESULT, firstRetryHash, 1L, 9L),
                createTestResult(SECOND_RESULT, secondRetryHash, 11L, 19L)
        );

        retryPlugin.aggregate(null, launchResultsList, null);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();

        assertThat(results).as("test results")
                .filteredOn(TestResult::isHidden)
                .hasSize(0);

        assertThat(results).as("test results with retries block")
                .flatExtracting(result -> result.getExtraBlock(RETRY_BLOCK_NAME))
                .hasSize(0);
    }

    /**
     * Verifies results without a generated retry hash are not merged.
     */
    @Description
    @Test
    void shouldIgnoreResultsWithoutRetryHash() {
        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                createTestResult(FIRST_RESULT, null, 1L, 9L),
                createTestResult(SECOND_RESULT, null, 11L, 19L)
        );

        retryPlugin.aggregate(null, launchResultsList, null);

        assertThat(launchResultsList.get(0).getAllResults())
                .filteredOn(TestResult::isHidden)
                .isEmpty();
    }

    /**
     * Verifies matching retry hashes merge attempts.
     */
    @Description
    @Test
    void shouldMergeByRetryHash() {
        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                createTestResult(FIRST_RESULT, "retry-hash", 1L, 9L),
                createTestResult(SECOND_RESULT, "retry-hash", 11L, 19L)
        );

        retryPlugin.aggregate(null, launchResultsList, null);

        assertThat(launchResultsList.get(0).getAllResults())
                .extracting(TestResult::getName, TestResult::isHidden, TestResult::getRetriesCount)
                .containsExactlyInAnyOrder(
                        tuple(FIRST_RESULT, true, 0),
                        tuple(SECOND_RESULT, false, 1)
                );
    }

    /**
     * Verifies retry aggregation keeps hidden results out of latest-result selection.
     */
    @Description
    @Test
    void shouldSkipHiddenResults() {
        String retryHash = UUID.randomUUID().toString();
        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                createTestResult(FIRST_RESULT, retryHash, 1L, 9L),
                createTestResult(SECOND_RESULT, retryHash, 11L, 19L),
                createTestResult(LAST_RESULT, retryHash, 21L, 29L).setHidden(true)
        );
        retryPlugin.aggregate(null, launchResultsList, null);
        Set<TestResult> results = launchResultsList.get(0).getAllResults();

        assertThat(results)
                .filteredOn(TestResult::isHidden)
                .extracting(TestResult::getName)
                .containsExactlyInAnyOrder(FIRST_RESULT, LAST_RESULT);

        assertThat(results)
                .filteredOn(result -> !result.isHidden())
                .extracting(TestResult::getName, TestResult::isFlaky, TestResult::getRetriesCount)
                .containsExactlyInAnyOrder(
                        tuple(SECOND_RESULT, false, 2)
                );
    }

    /**
     * Verifies passed retries do not mark the latest retry result as flaky.
     */
    @Description
    @Test
    void shouldNotMarkLatestAsFlakyIfRetriesArePassed() {
        String retryHash = UUID.randomUUID().toString();
        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                createTestResult(FIRST_RESULT, retryHash, 1L, 9L).setStatus(Status.PASSED),
                createTestResult(SECOND_RESULT, retryHash, 11L, 19L).setStatus(Status.PASSED)
        );
        retryPlugin.aggregate(null, launchResultsList, null);
        Set<TestResult> results = launchResultsList.get(0).getAllResults();

        assertThat(results)
                .filteredOn(TestResult::isHidden)
                .extracting(TestResult::getName)
                .containsExactlyInAnyOrder(FIRST_RESULT);

        assertThat(results)
                .filteredOn(result -> !result.isHidden())
                .extracting(TestResult::getName, TestResult::isFlaky)
                .containsExactlyInAnyOrder(tuple(SECOND_RESULT, false));
    }

    /**
     * Verifies skipped retries do not mark the latest retry result as flaky.
     */
    @Description
    @Test
    void shouldNotMarkLatestAsFlakyIfRetriesSkipped() {
        String retryHash = UUID.randomUUID().toString();
        List<LaunchResults> launchResultsList = createSingleLaunchResults(
                createTestResult(FIRST_RESULT, retryHash, 1L, 9L).setStatus(Status.SKIPPED),
                createTestResult(SECOND_RESULT, retryHash, 11L, 19L).setStatus(Status.PASSED),
                createTestResult(LAST_RESULT, retryHash, 12L, 20L).setHidden(true).setStatus(Status.PASSED)
        );
        retryPlugin.aggregate(null, launchResultsList, null);
        Set<TestResult> results = launchResultsList.get(0).getAllResults();

        assertThat(results)
                .filteredOn(TestResult::isHidden)
                .extracting(TestResult::getName)
                .containsExactlyInAnyOrder(FIRST_RESULT, LAST_RESULT);

        assertThat(results)
                .filteredOn(result -> !result.isHidden())
                .extracting(TestResult::getName, TestResult::isFlaky)
                .containsExactlyInAnyOrder(tuple(SECOND_RESULT, false));
    }

    private TestResult createTestResult(String name, String retryHash, long start, long stop) {
        return new TestResult()
                .setName(name)
                .setTestCaseHash(retryHash)
                .setParametersHash(PARAMETERS_HASH)
                .setStatus(Status.BROKEN)
                .setTime(new Time().setStart(start).setStop(stop));
    }
}
