package io.qameta.allure.retry;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.StatusDetails;
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
        byHistory.forEach((historyId, results) -> {
            final List<TestResult> sorted = results.stream()
                    .sorted(byTime())
                    .collect(Collectors.toList());
            if (sorted.size() > 1) {
                final TestResult first = sorted.remove(0);
                final List<RetryItem> retries = new ArrayList<>();
                first.addExtraBlock(RETRY_BLOCK_NAME, retries);
                for (TestResult result : sorted) {
                    result.setHidden(true);
                    retries.add(new RetryItem()
                            .withUid(result.getUid())
                            .withStatus(result.getStatus())
                            .withTime(result.getTime())
                    );
                }
                final StatusDetails details = first.getStatusDetails() != null
                        ? first.getStatusDetails() : new StatusDetails();
                first.setStatusDetails(details.withFlaky(true));
            }
        });
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
