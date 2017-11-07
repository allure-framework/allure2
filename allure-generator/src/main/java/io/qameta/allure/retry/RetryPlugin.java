package io.qameta.allure.retry;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;

import java.io.IOException;
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

import static io.qameta.allure.entity.TestResult.comparingByTimeDesc;

/**
 * The plugin that process test retries.
 *
 * @since 2.0
 */
public class RetryPlugin implements Aggregator {

    /* default */ static final String RETRY_BLOCK_NAME = "retries";

    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.AvoidInstantiatingObjectsInLoops"})
    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {

        Map<String, List<TestResult>> byHistory = launchesResults.stream()
                .flatMap(results -> results.getAllResults().stream())
                .filter(result -> Objects.nonNull(result.getHistoryId()))
                .collect(Collectors.toMap(TestResult::getHistoryId, Arrays::asList, this::merge));
        byHistory.forEach((historyId, results) ->
                findLatest(results).ifPresent(addRetries(results)));
    }

    private Consumer<TestResult> addRetries(final List<TestResult> results) {
        return latest -> {
            final List<RetryItem> retries = results.stream()
                    .sorted(comparingByTimeDesc())
                    .filter(result -> !latest.equals(result))
                    .map(this::prepareRetry)
                    .map(this::createRetryItem)
                    .collect(Collectors.toList());
            latest.setExtraBlock(RETRY_BLOCK_NAME, retries);
            final Set<Status> statuses = retries.stream()
                    .map(RetryItem::getStatus)
                    .distinct()
                    .collect(Collectors.toSet());

            statuses.remove(Status.PASSED);
            statuses.remove(Status.SKIPPED);

            latest.getStatusDetailsSafe().setFlaky(!statuses.isEmpty());
        };
    }

    private TestResult prepareRetry(TestResult result) {
        result.setHidden(true);
        result.setRetry(true);
        return result;
    }

    private RetryItem createRetryItem(TestResult result) {
        return new RetryItem()
                .setStatus(result.getStatus())
                .setStatusDetails(result.getStatusDetailsSafe().getMessage())
                .setTime(result.getTime())
                .setUid(result.getUid());
    }

    private Optional<TestResult> findLatest(final List<TestResult> results) {
        return results.stream()
                .filter(result -> !result.isHidden())
                .sorted(comparingByTimeDesc())
                .findFirst();
    }

    private List<TestResult> merge(final List<TestResult> first,
                                   final List<TestResult> second) {
        final List<TestResult> merged = new ArrayList<>();
        merged.addAll(first);
        merged.addAll(second);
        return merged;
    }
}
