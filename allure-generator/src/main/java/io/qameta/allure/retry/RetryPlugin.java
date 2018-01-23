package io.qameta.allure.retry;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.TestStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.EntityComparators.comparingTestResultsByStartAsc;
import static io.qameta.allure.entity.TestResult.comparingByTime;

/**
 * The plugin that process test retries.
 *
 * @since 2.0
 */
public class RetryPlugin implements Aggregator {

    protected static final String RETRY_BLOCK_NAME = "retries";

    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.AvoidInstantiatingObjectsInLoops"})
    @Override
    public void aggregate(final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {

        Map<String, List<TestResult>> byHistory = launchesResults.stream()
                .flatMap(results -> results.getAllResults().stream())
                .filter(result -> Objects.nonNull(result.getHistoryKey()))
                .collect(Collectors.toMap(TestResult::getHistoryKey, Arrays::asList, this::merge));
        byHistory.forEach((historyId, results) ->
                findLatest(results).ifPresent(addRetries(results)));
    }

    private Consumer<TestResult> addRetries(final List<TestResult> results) {
        return latest -> {
            final List<RetryItem> retries = results.stream()
                    .sorted(comparingTestResultsByStartAsc().reversed())
                    .filter(result -> !latest.equals(result))
                    .map(this::prepareRetry)
                    .map(this::createRetryItem)
                    .collect(Collectors.toList());
            latest.addExtraBlock(RETRY_BLOCK_NAME, retries);
            final Set<TestStatus> statuses = retries.stream()
                    .map(RetryItem::getStatus)
                    .distinct()
                    .collect(Collectors.toSet());

            statuses.remove(TestStatus.PASSED);
            statuses.remove(TestStatus.SKIPPED);

            latest.setFlaky(!statuses.isEmpty());
        };
    }

    private TestResult prepareRetry(final TestResult result) {
        result.setHidden(true);
        result.setRetry(true);
        return result;
    }

    private RetryItem createRetryItem(final TestResult result) {
        return new RetryItem()
                .setStatus(result.getStatus())
                .setStatusDetails(result.getMessage())
                .setStart(result.getStart())
                .setStop(result.getStop())
                .setDuration(result.getDuration())
                .setId(result.getId());
    }

    private Optional<TestResult> findLatest(final List<TestResult> results) {
        return results.stream()
                .filter(result -> !result.isHidden())
                .min(comparingByTime());
    }

    private List<TestResult> merge(final List<TestResult> first,
                                   final List<TestResult> second) {
        final List<TestResult> merged = new ArrayList<>();
        merged.addAll(first);
        merged.addAll(second);
        return merged;
    }
}
