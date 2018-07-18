package io.qameta.allure.retry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
 * Plugin that generates data for Retry-Trend graph.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class RetryTrendPlugin extends CompositeAggregator implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryTrendPlugin.class);

    private static final String JSON_FILE_NAME = "retry-trend.json";

    public static final String RETRY_TREND_BLOCK_NAME = "retry-trend";

    public RetryTrendPlugin() {
        super(Arrays.asList(
                new JsonAggregator(), new WidgetAggregator()
        ));
    }

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path historyFile = directory.resolve(Constants.HISTORY_DIR).resolve(JSON_FILE_NAME);
        if (Files.exists(historyFile)) {
            try (InputStream is = Files.newInputStream(historyFile)) {
                final ObjectMapper mapper = context.getValue();
                final JsonNode jsonNode = mapper.readTree(is);
                final List<RetryTrendItem> history = getStream(jsonNode)
                        .map(child -> parseItem(historyFile, mapper, child))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                visitor.visitExtra(RETRY_TREND_BLOCK_NAME, history);
            } catch (IOException e) {
                visitor.error("Could not read retry-trend file " + historyFile, e);
            }
        }
    }

    private Stream<JsonNode> getStream(final JsonNode jsonNode) {
        return stream(
                spliteratorUnknownSize(jsonNode.elements(), Spliterator.ORDERED),
                false);
    }

    private Optional<RetryTrendItem> parseItem(final Path historyFile,
                                               final ObjectMapper mapper,
                                               final JsonNode child) {
        try {
            return Optional.ofNullable(mapper.treeToValue(child, RetryTrendItem.class));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Could not read {}", historyFile, e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static List<RetryTrendItem> getData(final List<LaunchResults> launchesResults) {
        final RetryTrendItem item = createCurrent(launchesResults);
        final List<RetryTrendItem> data = getHistoryItems(launchesResults);

        return Stream.concat(Stream.of(item), data.stream())
                .limit(20)
                .collect(Collectors.toList());
    }

    private static List<RetryTrendItem> getHistoryItems(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(RetryTrendPlugin::getPreviousTrendData)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    private static List<RetryTrendItem> getPreviousTrendData(final LaunchResults results) {
        return results.getExtra(RETRY_TREND_BLOCK_NAME, ArrayList::new);
    }

    private static RetryTrendItem createCurrent(final List<LaunchResults> launchesResults) {
        final RetryTrendItem item = new RetryTrendItem();
        extractLatestExecutor(launchesResults).ifPresent(info -> {
            item.setBuildOrder(info.getBuildOrder());
            item.setReportName(info.getReportName());
            item.setReportUrl(info.getReportUrl());
        });
        launchesResults.stream()
                .flatMap(launch -> launch.getAllResults().stream())
                .forEach(item::update);
        return item;
    }

    private static Optional<ExecutorInfo> extractLatestExecutor(final List<LaunchResults> launches) {
        final Comparator<ExecutorInfo> comparator = comparing(ExecutorInfo::getBuildOrder, nullsFirst(naturalOrder()));
        return launches.stream()
                .map(launch -> launch.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .max(comparator);
    }

    /**
     * Generates retries trend data.
     */
    protected static class JsonAggregator extends CommonJsonAggregator {

        JsonAggregator() {
            super(Constants.HISTORY_DIR, JSON_FILE_NAME);
        }

        @Override
        protected List<RetryTrendItem> getData(final List<LaunchResults> launches) {
            return RetryTrendPlugin.getData(launches);
        }
    }

    /**
     * Generates widget data.
     */
    private static class WidgetAggregator extends CommonJsonAggregator {

        WidgetAggregator() {
            super(Constants.WIDGETS_DIR, JSON_FILE_NAME);
        }

        @Override
        public List<RetryTrendItem> getData(final List<LaunchResults> launches) {
            return RetryTrendPlugin.getData(launches);
        }
    }
}
