/*
 *  Copyright 2016-2024 Qameta Software Inc
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

import io.qameta.allure.Aggregator2;
import io.qameta.allure.Constants;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Statistic;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.qameta.allure.executor.ExecutorPlugin.getLatestExecutor;

/**
 * Plugins generates Summary widget.
 *
 * @since 2.0
 */
public class SummaryPlugin implements Aggregator2 {

    /**
     * Name of the json file.
     */
    protected static final String JSON_FILE_NAME = "summary.json";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        final SummaryData data1 = new SummaryData()
                .setStatistic(new Statistic())
                .setTime(new GroupTime())
                .setReportName(getReportName(configuration, launchesResults));

        launchesResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(result -> {
                    data1.getStatistic().update(result);
                    data1.getTime().update(result);
                });

        storage.addDataJson(String.format("%s/%s", Constants.WIDGETS_DIR, JSON_FILE_NAME), data1);
    }

    private static String getReportName(final Configuration configuration,
                                        final List<LaunchResults> launchesResults) {
        final String reportName = configuration.getReportName();
        if (Objects.nonNull(reportName)) {
            return reportName;
        }

        return getLatestExecutor(launchesResults)
                .map(ExecutorInfo::getReportName)
                .map(String::trim)
                .orElse(Constants.DEFAULT_REPORT_NAME);
    }

}
