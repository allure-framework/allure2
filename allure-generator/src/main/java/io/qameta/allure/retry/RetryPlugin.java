package io.qameta.allure.retry;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestCaseResult;
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

        Map<String, List<TestCaseResult>> byHistory = launchesResults.stream()
                .flatMap(results -> results.getAllResults().stream())
                .filter(result -> Objects.nonNull(result.getTestCaseId()))
                .collect(Collectors.toMap(TestCaseResult::getTestCaseId, Arrays::asList, this::merge));
        byHistory.forEach((historyId, results) -> {
            final List<TestCaseResult> sorted = results.stream()
                    .sorted(byTime())
                    .collect(Collectors.toList());
            if (sorted.size() > 1) {
                final TestCaseResult first = sorted.remove(0);
                final List<RetryItem> retries = new ArrayList<>();
                first.addExtraBlock(RETRY_BLOCK_NAME, retries);
                for (TestCaseResult result : sorted) {
                    result.setHidden(true);
                    retries.add(new RetryItem()
                            .withUid(result.getUid())
                            .withStatus(result.getStatus())
                            .withTime(result.getTime())
                    );
                }
            }
        });
    }

    private List<TestCaseResult> merge(final List<TestCaseResult> first,
                                       final List<TestCaseResult> second) {
        final List<TestCaseResult> merged = new ArrayList<>();
        merged.addAll(first);
        merged.addAll(second);
        return merged;
    }

    private Comparator<TestCaseResult> byTime() {
        return Comparator.comparing(
                TestCaseResult::getTime,
                Comparator.comparing(Time::getStart)
        ).reversed();
    }
}
