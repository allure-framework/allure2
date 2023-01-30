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
package io.qameta.allure.retry;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;

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

import static io.qameta.allure.entity.TestResult.comparingByTime;

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
                          final Path outputDirectory) {

        final Map<String, List<TestResult>> byHistory = launchesResults.stream()
                .flatMap(results -> results.getAllResults().stream())
                .filter(result -> Objects.nonNull(result.getHistoryId()))
                .collect(Collectors.toMap(TestResult::getHistoryId, Arrays::asList, this::merge));
        byHistory.forEach((historyId, results) ->
                findLatest(results).ifPresent(addRetries(results)));
    }

    private Consumer<TestResult> addRetries(final List<TestResult> results) {
        return latest -> {
            final List<RetryItem> retries = results.stream()
                    .sorted(comparingByTime())
                    .filter(result -> !latest.equals(result))
                    .map(this::prepareRetry)
                    .map(this::createRetryItem)
                    .collect(Collectors.toList());
            latest.addExtraBlock(RETRY_BLOCK_NAME, retries);
            final Set<Status> statuses = retries.stream()
                    .map(RetryItem::getStatus)
                    .filter(status -> !status.equals(latest.getStatus()))
                    .collect(Collectors.toSet());

            latest.setRetriesStatusChange(!statuses.isEmpty());
            latest.setRetriesCount(retries.size());
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
                .setStatusDetails(result.getStatusMessage())
                .setTime(result.getTime())
                .setUid(result.getUid());
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
