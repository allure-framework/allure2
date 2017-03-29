package io.qameta.allure.history;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Aggregator;
import io.qameta.allure.JacksonMapperContext;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.Processor;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.ResultsReader;
import io.qameta.allure.ResultsVisitor;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestCaseResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class HistoryPlugin implements ResultsReader, Processor, Aggregator {

    private static final String HISTORY_BLOCK_NAME = "history";

    private static final String HISTORY_FILE_NAME = "history.json";

    //@formatter:off
    private static final TypeReference<Map<String, HistoryData>> HISTORY_TYPE =
        new TypeReference<Map<String, HistoryData>>() {};
    //@formatter:on

    @Override
    public void readResults(ReportConfiguration configuration, ResultsVisitor visitor, Path directory) {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path historyFile = directory.resolve(HISTORY_FILE_NAME);
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
    public void process(ReportConfiguration configuration, List<LaunchResults> launches) {
        launches.forEach(launch -> {
            final Map<String, HistoryData> history = launch.getExtra(HISTORY_BLOCK_NAME, HashMap::new);
            launch.getResults().stream()
                    .filter(result -> Objects.nonNull(result.getTestCaseId()))
                    .filter(result -> history.containsKey(result.getTestCaseId()))
                    .forEach(result -> {
                        final HistoryData data = copy(history.get(result.getTestCaseId()));
                        data.updateStatistic(result);
                        result.addExtraBlock(HISTORY_BLOCK_NAME, data);
                    });
        });
    }

    @Override
    public void aggregate(ReportConfiguration configuration,
                          List<LaunchResults> launches,
                          Path outputDirectory) throws IOException {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path historyFolder = Files.createDirectories(outputDirectory.resolve(HISTORY_BLOCK_NAME));
        final Path historyFile = historyFolder.resolve(HISTORY_FILE_NAME);
        try (OutputStream os = Files.newOutputStream(historyFile)) {
            context.getValue().writeValue(os, getData(launches));
        }
    }

    protected Map<String, HistoryData> getData(List<LaunchResults> launches) {
        return launches.stream()
                .map(launch -> {
                    final Map<String, HistoryData> history = launch.getExtra(HISTORY_BLOCK_NAME, HashMap::new);

                    launch.getResults().stream()
                            .filter(result -> Objects.nonNull(result.getTestCaseId()))
                            .forEach(result -> updateHistory(history, result));
                    return history;
                })
                .reduce(new HashMap<>(), (a, b) -> {
                    a.putAll(b);
                    return a;
                });
    }

    private void updateHistory(Map<String, HistoryData> history, TestCaseResult result) {
        //@formatter:off
        final HistoryData data = history.computeIfAbsent(
            result.getTestCaseId(),
            id -> new HistoryData().withId(id).withName(result.getName())
        );
        //@formatter:on

        data.updateStatistic(result);

        final HistoryItem newItem = new HistoryItem()
                .withStatus(result.getStatus())
                .withStatusDetails(result.getStatusMessage().orElse(null))
                .withTime(result.getTime());

        final List<HistoryItem> newItems = Stream.concat(Stream.of(newItem), data.getItems().stream())
                .limit(5)
                .collect(Collectors.toList());
        data.setItems(newItems);
    }

    private static HistoryData copy(final HistoryData other) {
        final Statistic statistic = new Statistic();
        statistic.merge(other.getStatistic());
        final List<HistoryItem> items = new ArrayList<>(other.getItems());
        return new HistoryData()
                .withId(other.getId())
                .withName(other.getName())
                .withStatistic(statistic)
                .withItems(items);
    }
}
