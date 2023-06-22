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
package io.qameta.allure.core;

import io.qameta.allure.Aggregator2;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.entity.TestResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plugin that stores test results to report data folder.
 *
 * @since 2.0
 */
public class TestsResultsPlugin implements Aggregator2 {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        final List<TestResult> results = launchesResults.stream()
                .flatMap(launch -> launch.getAllResults().stream())
                .collect(Collectors.toList());
        for (TestResult result : results) {
            storage.addDataJson(String.format("data/test-cases/%s", result.getSource()), result);
        }
    }
}
