package io.qameta.allure.retry;

import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * eroshenkoam
 * 19.04.17
 */
public class RetryPluginTest {

    private static final String FIRST_RESULT = "first";

    private static final String SECOND_RESULT = "second";

    private static final String LAST_RESULT = "last";

    private RetryPlugin retryPlugin = new RetryPlugin();

    @Test
    public void shouldMergeRetriesTestResults() throws IOException {
        String historyId = UUID.randomUUID().toString();

        List<LaunchResults> launchResultsList = createLaunchResults(
                createTestResult(FIRST_RESULT, historyId, 1L, 9L),
                createTestResult(SECOND_RESULT, historyId, 11L, 19L),
                createTestResult(LAST_RESULT, historyId, 21L, 29L)
        );

        retryPlugin.aggregate(null, launchResultsList, null);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();

        assertThat(results).as("test retries")
                .filteredOn(TestResult::isHidden)
                .extracting(TestResult::getName)
                .containsExactlyInAnyOrder(FIRST_RESULT, SECOND_RESULT);

        TestResult lastResult = results.stream()
                .filter(r -> !r.isHidden()).findFirst().orElseGet(null);

        assertThat(lastResult).as("latest test result")
                .hasFieldOrPropertyWithValue("name", LAST_RESULT)
                .hasFieldOrPropertyWithValue("hidden", false)
                .hasFieldOrPropertyWithValue("statusDetails.flaky", true);

        assertThat(results).as("test results with retries block")
                .filteredOn(result -> result.getExtraBlock(RetryPlugin.RETRY_BLOCK_NAME) != null)
                .hasSize(1);

        List<RetryItem> retries = lastResult.getExtraBlock(RetryPlugin.RETRY_BLOCK_NAME);
        assertThat(retries).as("test results retries block")
                .isNotNull()
                .hasSize(2);
    }

    @Test
    public void shouldNotMergeOtherTestResults() throws IOException {
        String firstHistoryId = UUID.randomUUID().toString();
        String secondHistoryId = UUID.randomUUID().toString();

        List<LaunchResults> launchResultsList = createLaunchResults(
                createTestResult(FIRST_RESULT, firstHistoryId, 1L, 9L),
                createTestResult(SECOND_RESULT, secondHistoryId, 11L, 19L)
        );

        retryPlugin.aggregate(null, launchResultsList, null);

        Set<TestResult> results = launchResultsList.get(0).getAllResults();

        assertThat(results).as("test results")
                .filteredOn(TestResult::isHidden)
                .hasSize(0);

        assertThat(results).as("test results with retries block")
                .filteredOn(result -> result.getExtraBlock(RetryPlugin.RETRY_BLOCK_NAME) != null)
                .hasSize(0);
    }

    private List<LaunchResults> createLaunchResults(TestResult... input) {
        List<LaunchResults> launchResultsList = new ArrayList<>();
        launchResultsList.add(new DefaultLaunchResults(Arrays.stream(input).collect(Collectors.toSet()), null, null));
        return launchResultsList;
    }

    private TestResult createTestResult(String name, String historyId, long start, long stop) {
        return new TestResult().withName(name).withHistoryId(historyId).
                withTime(new Time().withStart(start).withStop(stop));
    }
}
