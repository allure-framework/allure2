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

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.executor.ExecutorPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin that adds history to the report.
 *
 * @since 2.0
 */
public class HistoryPlugin implements Reader, Aggregator {

    private static final Set<Status> MARK_STATUSES = new HashSet<>(Arrays.asList(
            Status.FAILED, Status.BROKEN, Status.PASSED
    ));

    private static final String HISTORY_BLOCK_NAME = "history";
    private static final String HISTORY_FILE_NAME = "history.json";

    //@formatter:off
    private static final TypeReference<Map<String, HistoryData>> HISTORY_TYPE =
            new TypeReference<Map<String, HistoryData>>() {
            };
    //@formatter:on

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path historyFile = directory.resolve(HISTORY_BLOCK_NAME).resolve(HISTORY_FILE_NAME);
        if (Files.exists(historyFile)) {
            try (InputStream is = Files.newInputStream(historyFile)) {
                final Map<String, HistoryData> history = context.getValue().readValue(is, HISTORY_TYPE);
                visitor.visitExtra(HISTORY_BLOCK_NAME, history);
            } catch (IOException e) {
                visitor.error("Could not read history file " + historyFile, e);
            }
        }
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path historyFolder = Files.createDirectories(outputDirectory.resolve(HISTORY_BLOCK_NAME));
        final Path historyFile = historyFolder.resolve(HISTORY_FILE_NAME);
        try (OutputStream os = Files.newOutputStream(historyFile)) {
            context.getValue().writeValue(os, getData(launchesResults));
        }
    }

    private boolean isNewFailed(final HistoryItem current,
                                final List<HistoryItem> prev) {
        return statusChangeTo(Status.FAILED, current, prev);
    }

    private boolean isNewBroken(final HistoryItem current,
                                final List<HistoryItem> prev) {
        return statusChangeTo(Status.BROKEN, current, prev);
    }

    private boolean isNewPassed(final HistoryItem current,
                                final List<HistoryItem> prev) {
        return statusChangeTo(Status.PASSED, current, prev);
    }

    private boolean statusChangeTo(final Status target,
                                   final HistoryItem current,
                                   final List<HistoryItem> prev) {
        final Optional<HistoryItem> prevItem = prev.stream()
                .filter(hi -> MARK_STATUSES.contains(hi.getStatus()))
                .findFirst();

        return prevItem.isPresent()
                && target.equals(current.getStatus())
                && !target.equals(prevItem.get().getStatus());
    }

    private boolean isFlaky(final HistoryItem current,
                            final List<HistoryItem> prev) {
        if (prev.isEmpty()) {
            return false;
        }
        if (current.getStatus() != Status.FAILED && current.getStatus() != Status.BROKEN) {
            return false;
        }
        final List<Status> statuses = prev
                .stream()
                .map(HistoryItem::getStatus)
                .limit(5)
                .collect(Collectors.toList());

        return statuses.contains(Status.PASSED)
                && statuses.indexOf(Status.PASSED) < statuses.lastIndexOf(Status.FAILED);
    }

    protected Map<String, HistoryData> getData(final List<LaunchResults> launches) {
        final Map<String, HistoryData> history = launches.stream()
                .map(launch -> launch.getExtra(HISTORY_BLOCK_NAME, (Supplier<Map<String, HistoryData>>) HashMap::new))
                .reduce(new HashMap<>(), (a, b) -> {
                    a.putAll(b);
                    return a;
                });
        launches.forEach(launch -> {
            final ExecutorInfo executorInfo = launch.getExtra(
                    ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                    ExecutorInfo::new
            );
            launch.getResults().stream()
                    .filter(result -> Objects.nonNull(result.getHistoryId()))
                    .forEach(result -> updateHistory(history, result, executorInfo));
        });
        return history;
    }

    private void updateHistory(final Map<String, HistoryData> history,
                               final TestResult result,
                               final ExecutorInfo info) {
        //@formatter:off
        final HistoryData data = history.computeIfAbsent(
                result.getHistoryId(),
                id -> new HistoryData().setStatistic(new Statistic())
        );
        //@formatter:on

        data.getStatistic().update(result);
        if (!data.getItems().isEmpty()) {
            result.addExtraBlock(HISTORY_BLOCK_NAME, copy(data));
        }
        final HistoryItem current = new HistoryItem()
                .setUid(result.getUid())
                .setStatus(result.getStatus())
                .setStatusDetails(result.getStatusMessage())
                .setTime(result.getTime());

        if (Objects.nonNull(info.getReportUrl())) {
            current.setReportUrl(createReportUrl(info.getReportUrl(), result.getUid()));
        }

        final List<HistoryItem> prevItems = data.getItems();
        result.setFlaky(result.isFlaky() || isFlaky(current, prevItems));
        result.setNewFailed(isNewFailed(current, prevItems));
        result.setNewBroken(isNewBroken(current, prevItems));
        result.setNewPassed(isNewPassed(current, prevItems));

        final List<HistoryItem> newItems = Stream.concat(Stream.of(current), prevItems.stream())
                .limit(20)
                .collect(Collectors.toList());
        data.setItems(newItems);
    }

    private static HistoryData copy(final HistoryData other) {
        final Statistic statistic = new Statistic();
        statistic.merge(other.getStatistic());
        final List<HistoryItem> items = new ArrayList<>(other.getItems());
        return new HistoryData()
                .setStatistic(statistic)
                .setItems(items);
    }

    private static String createReportUrl(final String reportUrl, final String uuid) {
        final String pattern = reportUrl.endsWith("index.html") ? "%s#testresult/%s" : "%s/#testresult/%s";
        return String.format(pattern, reportUrl, uuid);
    }
}
