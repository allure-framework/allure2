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
package io.qameta.allure.summary;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Statistic;

import java.util.List;

/**
 * Plugins generates Summary widget.
 *
 * @since 2.0
 */
public class SummaryPlugin extends CommonJsonAggregator {

    /**
     * Name of the json file.
     */
    protected static final String JSON_FILE_NAME = "summary.json";

    public SummaryPlugin() {
        super(Constants.WIDGETS_DIR, JSON_FILE_NAME);
    }

    @Override
    protected SummaryData getData(final List<LaunchResults> launches) {
        final SummaryData data = new SummaryData()
                .setStatistic(new Statistic())
                .setTime(new GroupTime())
                .setReportName("Allure Report");
        launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .forEach(result -> {
                    data.getStatistic().update(result);
                    data.getTime().update(result);
                });
        return data;
    }
}
