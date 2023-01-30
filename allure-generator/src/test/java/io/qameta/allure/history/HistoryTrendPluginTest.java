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

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.OutputStream;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class HistoryTrendPluginTest {

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
        plugin.readResults(configuration, visitor, resultsDirectory);

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(visitor, times(1))
                .visitExtra(eq(HISTORY_TREND_BLOCK_NAME), captor.capture());

        assertThat(captor.getValue())
                .hasSize(4)
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal)
                .containsExactly(20L, 12L, 12L, 1L);
    }

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
        plugin.readResults(configuration, visitor, resultsDirectory);

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.forClass(List.class);
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

    @Test
    void shouldProcessCorruptedData(@TempDir final Path resultsDirectory) throws Exception {
        final Path history = Files.createDirectories(resultsDirectory.resolve("history"));
        Files.createFile(history.resolve("history-trend.json"));

        final Configuration configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(new JacksonContext());

        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        plugin.readResults(configuration, visitor, resultsDirectory);

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(visitor, times(1))
                .visitExtra(eq(HISTORY_TREND_BLOCK_NAME), captor.capture());

        assertThat(captor.getValue()).hasSize(0);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAggregateForEmptyReport(@TempDir final Path outputDirectory) throws Exception {
        final Configuration configuration = mock(Configuration.class);
        final JacksonContext context = mock(JacksonContext.class);
        final ObjectMapper mapper = mock(ObjectMapper.class);

        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(context);

        when(context.getValue())
                .thenReturn(mapper);

        final HistoryTrendPlugin.JsonAggregator aggregator = new HistoryTrendPlugin.JsonAggregator();
        aggregator.aggregate(configuration, Collections.emptyList(), outputDirectory);

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(mapper, times(1))
                .writeValue(any(OutputStream.class), captor.capture());

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

    @SuppressWarnings("unchecked")
    @Test
    void shouldGetData() {
        final List<HistoryTrendItem> history = randomHistoryTrendItems();
        final List<HistoryTrendItem> data = HistoryTrendPlugin.getData(createSingleLaunchResults(
                singletonMap(HISTORY_TREND_BLOCK_NAME, history),
                randomTestResult().setStatus(Status.PASSED),
                randomTestResult().setStatus(Status.FAILED),
                randomTestResult().setStatus(Status.FAILED)
        ));

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

        final List<HistoryTrendItem> data = new HistoryTrendPlugin().getData(launchResults);

        assertThat(data)
                .hasSize(1 + history1.size() + history2.size());

        final HistoryTrendItem historyTrendItem = data.get(0);

        assertThat(historyTrendItem)
                .hasFieldOrPropertyWithValue("buildOrder", 7L);
    }

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
        final List<HistoryTrendItem> data = HistoryTrendPlugin.getData(launchResults);

        assertThat(data)
                .hasSize(1 + 2 * history.size());
    }
}
