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
package io.qameta.allure.history;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.testdata.TestData;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTORS_BLOCK_NAME;
import static io.qameta.allure.history.HistoryTrendPlugin.HISTORY_TREND_BLOCK_NAME;
import static io.qameta.allure.history.HistoryTrendPlugin.JSON_FILE_NAME;
import static io.qameta.allure.testdata.TestData.createLaunchResults;
import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static io.qameta.allure.testdata.TestData.randomHistoryTrendItems;
import static io.qameta.allure.testdata.TestData.randomTestResult;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class HistoryTrendPluginTest {

    /**
     * Verifies reading old data for history trend aggregation.
     */
    @Description
    @SuppressWarnings("unchecked")
    @Test
    void shouldReadOldData(@TempDir final Path resultsDirectory) throws Exception {
        final Path history = Files.createDirectories(resultsDirectory.resolve("history"));
        final Path trend = history.resolve(JSON_FILE_NAME);
        TestData.unpackFile("history-trend-old.json", trend);

        final Configuration configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(new JacksonContext());

        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        Allure.step("Read old history trend data from " + trend, () -> plugin.readResults(
                configuration,
                visitor,
                resultsDirectory
        ));

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.captor();
        verify(visitor, times(1))
                .visitExtra(eq(HISTORY_TREND_BLOCK_NAME), captor.capture());

        assertThat(captor.getValue())
                .hasSize(4)
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal)
                .containsExactly(20L, 12L, 12L, 1L);
    }

    /**
     * Verifies reading new data for history trend aggregation.
     */
    @Description
    @SuppressWarnings("unchecked")
    @Test
    void shouldReadNewData(@TempDir final Path resultsDirectory) throws Exception {
        final Path history = Files.createDirectories(resultsDirectory.resolve("history"));
        final Path trend = history.resolve(JSON_FILE_NAME);
        TestData.unpackFile("history-trend.json", trend);

        final Configuration configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(new JacksonContext());

        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        Allure.step("Read current history trend data from " + trend, () -> plugin.readResults(
                configuration,
                visitor,
                resultsDirectory
        ));

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.captor();
        verify(visitor, times(1))
                .visitExtra(eq(HISTORY_TREND_BLOCK_NAME), captor.capture());

        assertThat(captor.getValue())
                .hasSize(4)
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal)
                .containsExactly(20L, 12L, 12L, 1L);

        assertThat(captor.getValue())
                .hasSize(4)
                .extracting(HistoryTrendItem::getBuildOrder,
                        HistoryTrendItem::getReportName, HistoryTrendItem::getReportUrl)
                .containsExactly(
                        Tuple.tuple(7L, "some", "some/report#7"),
                        Tuple.tuple(6L, "some", "some/report#6"),
                        Tuple.tuple(5L, "some", "some/report#5"),
                        Tuple.tuple(4L, "some", "some/report#4")
                );
    }

    /**
     * Verifies processing corrupted data for history trend aggregation.
     */
    @Description
    @SuppressWarnings("unchecked")
    @Test
    void shouldProcessCorruptedData(@TempDir final Path resultsDirectory) throws Exception {
        final Path history = Files.createDirectories(resultsDirectory.resolve("history"));
        final Path trend = history.resolve("history-trend.json");
        Allure.step("Create empty history trend file", () -> Files.createFile(trend));

        final Configuration configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(new JacksonContext());

        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        Allure.step("Read corrupted history trend data from " + trend, () -> plugin.readResults(
                configuration,
                visitor,
                resultsDirectory
        ));

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.captor();
        verify(visitor, times(1))
                .visitExtra(eq(HISTORY_TREND_BLOCK_NAME), captor.capture());

        assertThat(captor.getValue()).hasSize(0);
    }

    /**
     * Verifies aggregating for empty report for history trend aggregation.
     */
    @Description
    @Test
    void shouldAggregateForEmptyReport() {
        final Configuration configuration = mock(Configuration.class);

        final HistoryTrendPlugin.JsonAggregator aggregator = new HistoryTrendPlugin.JsonAggregator();
        final ReportStorage reportStorage = mock();
        Allure.step(
                "Aggregate history trend widget for an empty report",
                () -> aggregator.aggregate(configuration, Collections.emptyList(), reportStorage)
        );


        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.captor();
        verify(reportStorage, times(1)).addDataJson(eq("history/history-trend.json"), captor.capture());

        assertThat(captor.getValue())
                .hasSize(1)
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal)
                .containsExactly(0L);

        assertThat(captor.getValue())
                .hasSize(1)
                .extracting(HistoryTrendItem::getBuildOrder,
                        HistoryTrendItem::getReportName, HistoryTrendItem::getReportUrl)
                .containsExactly(Tuple.tuple(null, null, null));

    }

    /**
     * Verifies resolving data for history trend aggregation.
     */
    @Description
    @Test
    void shouldGetData() {
        final List<HistoryTrendItem> history = randomHistoryTrendItems();
        final List<HistoryTrendItem> data = Allure.step(
                "Build history trend data with previous trend entries and three current results",
                () -> HistoryTrendPlugin.getData(createSingleLaunchResults(
                        singletonMap(HISTORY_TREND_BLOCK_NAME, history),
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                ))
        );

        assertThat(data)
                .hasSize(1 + history.size())
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal, Statistic::getFailed, Statistic::getPassed)
                .first()
                .isEqualTo(Tuple.tuple(3L, 2L, 1L));

        final List<HistoryTrendItem> next = data.subList(1, data.size());

        assertThat(next)
                .containsExactlyElementsOf(history);

    }

    /**
     * Verifies finding latest executor for history trend aggregation.
     */
    @Description
    @Test
    void shouldFindLatestExecutor() {
        final Map<String, Object> extra1 = new HashMap<>();
        final List<HistoryTrendItem> history1 = randomHistoryTrendItems();
        extra1.put(HISTORY_TREND_BLOCK_NAME, history1);
        extra1.put(EXECUTORS_BLOCK_NAME, new ExecutorInfo().setBuildOrder(1L));
        final Map<String, Object> extra2 = new HashMap<>();
        final List<HistoryTrendItem> history2 = randomHistoryTrendItems();
        extra2.put(HISTORY_TREND_BLOCK_NAME, history2);
        extra2.put(EXECUTORS_BLOCK_NAME, new ExecutorInfo().setBuildOrder(7L));

        final List<LaunchResults> launchResults = Arrays.asList(
                createLaunchResults(extra1,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                ),
                createLaunchResults(extra2,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                )
        );

        final List<HistoryTrendItem> data = Allure.step(
                "Build history trend data and choose the latest executor",
                () -> HistoryTrendPlugin.getData(launchResults)
        );

        assertThat(data)
                .hasSize(1 + history1.size() + history2.size());

        final HistoryTrendItem historyTrendItem = data.get(0);

        assertThat(historyTrendItem)
                .hasFieldOrPropertyWithValue("buildOrder", 7L);
    }

    /**
     * Verifies processing null build order for history trend aggregation.
     */
    @Description
    @Test
    void shouldProcessNullBuildOrder() {
        final List<HistoryTrendItem> history = randomHistoryTrendItems();
        final Map<String, Object> extra = new HashMap<>();
        extra.put(HISTORY_TREND_BLOCK_NAME, history);
        extra.put(EXECUTORS_BLOCK_NAME, new ExecutorInfo().setBuildOrder(null));

        final List<LaunchResults> launchResults = Arrays.asList(
                createLaunchResults(extra,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                ),
                createLaunchResults(extra,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                )
        );
        final List<HistoryTrendItem> data = Allure.step(
                "Build history trend data with null build order",
                () -> HistoryTrendPlugin.getData(launchResults)
        );

        assertThat(data)
                .hasSize(1 + 2 * history.size());
    }
}
