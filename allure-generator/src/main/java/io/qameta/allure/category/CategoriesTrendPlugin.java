package io.qameta.allure.category;

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
 * Plugin that generates data for Categories-trend graph.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class CategoriesTrendPlugin extends CompositeAggregator implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoriesTrendPlugin.class);

    private static final String JSON_FILE_NAME = "categories-trend.json";

    public static final String CATEGORIES_TREND_BLOCK_NAME = "categories-trend";

    public CategoriesTrendPlugin() {
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
                final List<CategoriesTrendItem> history = getStream(jsonNode)
                        .map(child -> parseItem(historyFile, mapper, child))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                visitor.visitExtra(CATEGORIES_TREND_BLOCK_NAME, history);
            } catch (IOException e) {
                visitor.error("Could not read categories-trend file " + historyFile, e);
            }
        }
    }

    private Stream<JsonNode> getStream(final JsonNode jsonNode) {
        return stream(
                spliteratorUnknownSize(jsonNode.elements(), Spliterator.ORDERED),
                false);
    }

    private Optional<CategoriesTrendItem> parseItem(final Path historyFile,
                                                    final ObjectMapper mapper,
                                                    final JsonNode child) {
        try {
            return Optional.ofNullable(mapper.treeToValue(child, CategoriesTrendItem.class));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Could not read {}", historyFile, e);
            return Optional.empty();
        }
    }

    protected static List<CategoriesTrendItem> getData(final List<LaunchResults> launchesResults) {
        final CategoriesTrendItem item = createCurrent(launchesResults);
        final List<CategoriesTrendItem> data = getHistoryItems(launchesResults);

        return Stream.concat(Stream.of(item), data.stream())
                .limit(20)
                .collect(Collectors.toList());
    }

    private static List<CategoriesTrendItem> getHistoryItems(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(CategoriesTrendPlugin::getPreviousTrendData)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    private static List<CategoriesTrendItem> getPreviousTrendData(final LaunchResults results) {
        return results.getExtra(CATEGORIES_TREND_BLOCK_NAME, ArrayList::new);
    }

    private static CategoriesTrendItem createCurrent(final List<LaunchResults> launchesResults) {
        final CategoriesTrendItem item = new CategoriesTrendItem();
        extractLatestExecutor(launchesResults).ifPresent(info -> {
            item.setBuildOrder(info.getBuildOrder());
            item.setReportName(info.getReportName());
            item.setReportUrl(info.getReportUrl());
        });
        launchesResults.stream()
                .flatMap(launch -> launch.getAllResults().stream())
                .forEach(item::increaseCategories);
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
     * Generates history trend data.
     */
    private static class JsonAggregator extends CommonJsonAggregator {

        JsonAggregator() {
            super(Constants.HISTORY_DIR, JSON_FILE_NAME);
        }

        @Override
        protected List<CategoriesTrendItem> getData(final List<LaunchResults> launches) {
            return CategoriesTrendPlugin.getData(launches);
        }
    }

    /**
     * Generates widget data.
     */
    private static class WidgetAggregator extends CommonJsonAggregator {

        WidgetAggregator() {
            super("widgets", JSON_FILE_NAME);
        }

        @Override
        public List<CategoriesTrendItem> getData(final List<LaunchResults> launches) {
            return CategoriesTrendPlugin.getData(launches);
        }
    }
}
