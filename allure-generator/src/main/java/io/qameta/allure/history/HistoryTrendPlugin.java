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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.trend.AbstractTrendPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin that adds history trend widget.
 *
 * @since 2.0
 */
public class HistoryTrendPlugin extends AbstractTrendPlugin<HistoryTrendItem> {

    public static final String JSON_FILE_NAME = "history-trend.json";

    public static final String HISTORY_TREND_BLOCK_NAME = "history-trend";

    public HistoryTrendPlugin() {
        super(Arrays.asList(new JsonAggregator(), new WidgetAggregator()), JSON_FILE_NAME, HISTORY_TREND_BLOCK_NAME);
    }

    @Override
    protected Optional<HistoryTrendItem> parseItem(final ObjectMapper mapper, final JsonNode child)
            throws JsonProcessingException {

        if (Objects.nonNull(child.get("total"))) {
            final Statistic statistic = mapper.treeToValue(child, Statistic.class);
            return Optional.of(new HistoryTrendItem().setStatistic(statistic));
        }
        return Optional.ofNullable(mapper.treeToValue(child, HistoryTrendItem.class));
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static List<HistoryTrendItem> getData(final List<LaunchResults> launchesResults) {
        final HistoryTrendItem item = createCurrent(launchesResults);
        final List<HistoryTrendItem> data = getHistoryItems(launchesResults);

        return Stream.concat(Stream.of(item), data.stream())
                .limit(20)
                .collect(Collectors.toList());
    }

    private static HistoryTrendItem createCurrent(final List<LaunchResults> launchesResults) {
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

    private static List<HistoryTrendItem> getHistoryItems(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(HistoryTrendPlugin::getPreviousTrendData)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    private static List<HistoryTrendItem> getPreviousTrendData(final LaunchResults results) {
        return results.getExtra(HISTORY_TREND_BLOCK_NAME, ArrayList::new);
    }

    /**
     * Generates history trend data.
     */
    protected static class JsonAggregator extends CommonJsonAggregator {

        JsonAggregator() {
            super(Constants.HISTORY_DIR, JSON_FILE_NAME);
        }

        @Override
        protected List<HistoryTrendItem> getData(final List<LaunchResults> launches) {
            return HistoryTrendPlugin.getData(launches);
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
        public List<HistoryTrendItem> getData(final List<LaunchResults> launches) {
            return HistoryTrendPlugin.getData(launches);
        }
    }
}
