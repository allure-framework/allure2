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
package io.qameta.allure.category;

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
 * Plugin that generates data for Categories-trend graph.
 */
public class CategoriesTrendPlugin extends AbstractTrendPlugin<CategoriesTrendItem> {

    private static final String JSON_FILE_NAME = "categories-trend.json";

    public static final String CATEGORIES_TREND_BLOCK_NAME = "categories-trend";

    public CategoriesTrendPlugin() {
        super(Arrays.asList(new JsonAggregator(), new WidgetAggregator()), JSON_FILE_NAME, CATEGORIES_TREND_BLOCK_NAME);
    }

    @Override
    protected Optional<CategoriesTrendItem> parseItem(final ObjectMapper mapper, final JsonNode child)
            throws JsonProcessingException {
        return Optional.ofNullable(mapper.treeToValue(child, CategoriesTrendItem.class));
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
            super(Constants.WIDGETS_DIR, JSON_FILE_NAME);
        }

        @Override
        public List<CategoriesTrendItem> getData(final List<LaunchResults> launches) {
            return CategoriesTrendPlugin.getData(launches);
        }
    }
}
