package io.qameta.allure.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTORS_BLOCK_NAME;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
 * Plugin that adds history trend widget.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class HistoryTrendPlugin implements Reader, Aggregator, Widget {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryTrendPlugin.class);

    public static final String HISTORY_TREND_JSON = "history-trend.json";
    public static final String HISTORY_TREND_BLOCK_NAME = "history-trend";

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path historyFile = directory.resolve("history").resolve(HISTORY_TREND_JSON);

        if (Files.exists(historyFile)) {
            try (InputStream is = Files.newInputStream(historyFile)) {
                final ObjectMapper mapper = context.getValue();
                final JsonNode jsonNode = mapper.readTree(is);
                final List<HistoryTrendItem> history = getStream(jsonNode)
                        .map(child -> parseItem(historyFile, mapper, child))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                visitor.visitExtra(HISTORY_TREND_BLOCK_NAME, history);
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
    public List<HistoryTrendItem> getData(final Configuration configuration, final List<LaunchResults> launches) {
        return getHistoryTrendData(launches);
    }

    @Override
    public String getName() {
        return HISTORY_TREND_BLOCK_NAME;
    }

    private Stream<JsonNode> getStream(final JsonNode jsonNode) {
        return stream(
                spliteratorUnknownSize(jsonNode.elements(), Spliterator.ORDERED),
                false);
    }

    private Optional<HistoryTrendItem> parseItem(final Path historyFile,
                                                 final ObjectMapper mapper, final JsonNode child) {
        try {

            if (Objects.nonNull(child.get("total"))) {
                final Statistic statistic = mapper.treeToValue(child, Statistic.class);
                return Optional.of(new HistoryTrendItem().setStatistic(statistic));

            }
            return Optional.ofNullable(mapper.treeToValue(child, HistoryTrendItem.class));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Could not read {}", historyFile, e);
            return Optional.empty();
        }
    }

    private List<HistoryTrendItem> getHistoryTrendData(final List<LaunchResults> launchesResults) {
        final HistoryTrendItem item = createCurrent(launchesResults);
        final List<HistoryTrendItem> data = getHistoryItems(launchesResults);

        return Stream.concat(Stream.of(item), data.stream())
                .limit(20)
                .collect(Collectors.toList());
    }

    private HistoryTrendItem createCurrent(final List<LaunchResults> launchesResults) {
        final Statistic statistic = launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .map(TestResult::getStatus)
                .collect(Statistic::new, Statistic::update, Statistic::merge);
        final HistoryTrendItem item = new HistoryTrendItem()
                .setStatistic(statistic);
        extractLatestExecutor(launchesResults).ifPresent(info -> {
            item.setBuildOrder(info.getBuildOrder());
            item.setReportName(info.getReportName());
            item.setReportUrl(info.getReportUrl());
        });
        return item;
    }

    private List<HistoryTrendItem> getHistoryItems(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(this::getPreviousTrendData)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    private List<HistoryTrendItem> getPreviousTrendData(final LaunchResults results) {
        return results.getExtra(HISTORY_TREND_BLOCK_NAME, ArrayList::new);
    }

    private static Optional<ExecutorInfo> extractLatestExecutor(final List<LaunchResults> launches) {
        final Comparator<ExecutorInfo> comparator = comparing(ExecutorInfo::getBuildOrder, nullsFirst(naturalOrder()));
        return launches.stream()
                .map(launch -> launch.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .sorted(comparator.reversed())
                .findFirst();
    }
}
