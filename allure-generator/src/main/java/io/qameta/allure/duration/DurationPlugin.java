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

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.severity.SeverityPlugin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that generates data for Duration graph.
 *
 * @since 2.0
 */
public class DurationPlugin extends CommonJsonAggregator {

    public DurationPlugin() {
        super(Constants.WIDGETS_DIR, "duration.json");
    }

    @Override
    protected List<DurationData> getData(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .flatMap(launch -> launch.getResults().stream())
                .map(this::createData)
                .collect(Collectors.toList());
    }

    private DurationData createData(final TestResult result) {
        return new DurationData()
                .setUid(result.getUid())
                .setName(result.getName())
                .setStatus(result.getStatus())
                .setTime(result.getTime())
                .setSeverity(result.getExtraBlock(SeverityPlugin.SEVERITY_BLOCK_NAME));
    }
}
