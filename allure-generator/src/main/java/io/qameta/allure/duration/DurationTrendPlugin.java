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
package io.qameta.allure.duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.trend.AbstractTrendPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin that generates data for Duration-Trend graph.
 */
public class DurationTrendPlugin extends AbstractTrendPlugin<DurationTrendItem> {

    protected static final String JSON_FILE_NAME = "duration-trend.json";

    private static final String DURATION_TREND_BLOCK_NAME = "duration-trend";

    public DurationTrendPlugin() {
        super(Arrays.asList(new JsonAggregator(), new WidgetAggregator()), JSON_FILE_NAME, DURATION_TREND_BLOCK_NAME);
    }

    @Override
    protected Optional<DurationTrendItem> parseItem(final ObjectMapper mapper, final JsonNode child)
            throws JsonProcessingException {
        return Optional.ofNullable(mapper.treeToValue(child, DurationTrendItem.class));
    }

    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static List<DurationTrendItem> getData(final List<LaunchResults> launchesResults) {
        final DurationTrendItem item = createCurrent(launchesResults);
        final List<DurationTrendItem> data = getHistoryItems(launchesResults);

        return Stream.concat(Stream.of(item), data.stream())
                .limit(20)
                .collect(Collectors.toList());
    }

    private static List<DurationTrendItem> getHistoryItems(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(DurationTrendPlugin::getPreviousTrendData)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    private static List<DurationTrendItem> getPreviousTrendData(final LaunchResults results) {
        return results.getExtra(DURATION_TREND_BLOCK_NAME, ArrayList::new);
    }

    private static DurationTrendItem createCurrent(final List<LaunchResults> launchesResults) {
        final DurationTrendItem item = new DurationTrendItem();
        extractLatestExecutor(launchesResults).ifPresent(info -> {
            item.setBuildOrder(info.getBuildOrder());
            item.setReportName(info.getReportName());
            item.setReportUrl(info.getReportUrl());
        });
        launchesResults.stream()
                .flatMap(launch -> launch.getResults().stream())
                .forEach(item::updateTime);
        return item;
    }

    /**
     * Generates tree data.
     */
    private static class JsonAggregator extends CommonJsonAggregator {

        JsonAggregator() {
            super(Constants.HISTORY_DIR, JSON_FILE_NAME);
        }

        @Override
        protected List<DurationTrendItem> getData(final List<LaunchResults> launches) {
            return DurationTrendPlugin.getData(launches);
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
        public List<DurationTrendItem> getData(final List<LaunchResults> launches) {
            return DurationTrendPlugin.getData(launches);
        }
    }
}
