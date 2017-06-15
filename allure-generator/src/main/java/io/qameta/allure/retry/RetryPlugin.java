package io.qameta.allure.retry;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * The plugin that process test retries.
 *
 * @since 2.0
 */
public class RetryPlugin implements Aggregator {

    public static final String RETRY_BLOCK_NAME = "retries";

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
                    .sorted(byTime())
                    .filter(result -> !latest.equals(result))
                    .map(retry -> retry.withHidden(true))
                    .map(retry -> new RetryItem()
                            .withStatus(retry.getStatus())
                            .withStatusDetails(retry.getStatusDetailsSafe().getMessage())
                            .withTime(retry.getTime())
                            .withUid(retry.getUid()))
                    .collect(Collectors.toList());
            latest.addExtraBlock(RETRY_BLOCK_NAME, retries);
            final Set<Status> statuses = retries.stream()
                    .map(RetryItem::getStatus)
                    .distinct()
                    .collect(Collectors.toSet());

            statuses.remove(Status.PASSED);
            statuses.remove(Status.SKIPPED);

            latest.getStatusDetailsSafe().setFlaky(!statuses.isEmpty());
        };
    }

    private Optional<TestResult> findLatest(final List<TestResult> results) {
        return results.stream()
                .filter(result -> !result.isHidden())
                .sorted(byTime())
                .findFirst();
    }

    private List<TestResult> merge(final List<TestResult> first,
                                   final List<TestResult> second) {
        final List<TestResult> merged = new ArrayList<>();
        merged.addAll(first);
        merged.addAll(second);
        return merged;
    }

    private Comparator<TestResult> byTime() {
        return comparing(
                TestResult::getTime,
                nullsFirst(comparing(Time::getStart, nullsFirst(naturalOrder())))
        ).reversed();
    }
}
