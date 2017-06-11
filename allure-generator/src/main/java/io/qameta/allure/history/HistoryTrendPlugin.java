package io.qameta.allure.history;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.ExtraStatisticMethods;
import io.qameta.allure.entity.HistoryTrendItem;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin that adds history trend widget.
 *
 * @since 2.0
 */
public class HistoryTrendPlugin implements Reader, Aggregator, Widget {

    private static final String HISTORY_TREND_JSON = "history-trend.json";
    private static final String HISTORY_TREND = "history-trend";

    //@formatter:off
    private static final TypeReference<List<HistoryTrendItem>> HISTORY_TYPE =
        new TypeReference<List<HistoryTrendItem>>() {};
    //@formatter:on

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path historyFile = directory.resolve("history").resolve(HISTORY_TREND_JSON);

        if (Files.exists(historyFile)) {
            try (InputStream is = Files.newInputStream(historyFile)) {
                final List<HistoryTrendItem> history = context.getValue().readValue(is, HISTORY_TYPE);
                visitor.visitExtra(HISTORY_TREND, history);
            } catch (IOException e) {
                visitor.error("Could not read history-trend file " + historyFile, e);
            }
        }
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final List<HistoryTrendItem> limited = getHistoryTrendData(launchesResults);
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path historyFolder = Files.createDirectories(outputDirectory.resolve("history"));
        final Path historyFile = historyFolder.resolve(HISTORY_TREND_JSON);
        try (OutputStream os = Files.newOutputStream(historyFile)) {
            context.getValue().writeValue(os, limited);
        }
    }

    @Override
    public Object getData(final Configuration configuration, final List<LaunchResults> launches) {
        return getHistoryTrendData(launches);
    }

    @Override
    public String getName() {
        return HISTORY_TREND;
    }

    private List<HistoryTrendItem> getHistoryTrendData(final List<LaunchResults> launchesResults) {
        final Statistic statistic = launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .map(TestResult::getStatus)
                .collect(Statistic::new, ExtraStatisticMethods::update, ExtraStatisticMethods::merge);
        final HistoryTrendItem item = new HistoryTrendItem()
                .withStatistics(statistic);
        extractLatestExecutor(launchesResults).ifPresent(item::setExecutorInfo);

        final List<HistoryTrendItem> data = launchesResults.stream()
                .map(results -> results.getExtra(HISTORY_TREND, ArrayList<HistoryTrendItem>::new))
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });

        return Stream.concat(
                Stream.of(item),
                data.stream()
        ).limit(20).collect(Collectors.toList());
    }

    private static Optional<ExecutorInfo> extractLatestExecutor(final List<LaunchResults> launches) {
        final Comparator<ExecutorInfo> comparator = Comparator.comparing(e -> Integer.valueOf(e.getBuildId()));
        return launches.stream()
                .map(launch -> launch.getExtra("executor"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .sorted(comparator.reversed())
                .findFirst();
    }
}
