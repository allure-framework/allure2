package io.qameta.allure.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.testdata.TestData;
import org.assertj.core.groups.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static io.qameta.allure.history.HistoryTrendPlugin.HISTORY_TREND;
import static io.qameta.allure.history.HistoryTrendPlugin.HISTORY_TREND_JSON;
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
public class HistoryTrendPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReadOldData() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path history = Files.createDirectories(resultsDirectory.resolve("history"));
        final Path trend = history.resolve(HISTORY_TREND_JSON);
        TestData.unpackFile("history-trend-old.json", trend);

        final Configuration configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(new JacksonContext());

        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        plugin.readResults(configuration, visitor, resultsDirectory);

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(visitor, times(1))
                .visitExtra(eq(HISTORY_TREND), captor.capture());

        assertThat(captor.getValue())
                .hasSize(4)
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal)
                .containsExactly(20L, 12L, 12L, 1L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReadNewData() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path history = Files.createDirectories(resultsDirectory.resolve("history"));
        final Path trend = history.resolve(HISTORY_TREND_JSON);
        TestData.unpackFile("history-trend.json", trend);

        final Configuration configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(new JacksonContext());

        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        plugin.readResults(configuration, visitor, resultsDirectory);

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(visitor, times(1))
                .visitExtra(eq(HISTORY_TREND), captor.capture());

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

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAggregateForEmptyReport() throws Exception {
        final Path outputDirectory = folder.newFolder().toPath();

        final Configuration configuration = mock(Configuration.class);
        final JacksonContext context = mock(JacksonContext.class);
        final ObjectMapper mapper = mock(ObjectMapper.class);

        when(configuration.requireContext(JacksonContext.class))
                .thenReturn(context);

        when(context.getValue())
                .thenReturn(mapper);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        plugin.aggregate(configuration, Collections.emptyList(), outputDirectory);

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

    @Test
    public void shouldGetData() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final List<HistoryTrendItem> history = randomHistoryTrendItems();
        final List<HistoryTrendItem> data = new HistoryTrendPlugin().getData(configuration, createSingleLaunchResults(
                singletonMap(HistoryTrendPlugin.HISTORY_TREND, history),
                randomTestResult().withStatus(Status.PASSED),
                randomTestResult().withStatus(Status.FAILED),
                randomTestResult().withStatus(Status.FAILED)
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
}