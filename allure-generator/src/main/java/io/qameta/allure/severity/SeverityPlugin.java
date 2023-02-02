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
package io.qameta.allure.severity;

import io.qameta.allure.Aggregator;
import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.CompositeAggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.TestResult;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.allure.entity.LabelName.SEVERITY;

/**
 * Plugin that adds severity information to tests results.
 *
 * @since 2.0
 */
public class SeverityPlugin extends CompositeAggregator {

    public static final String SEVERITY_BLOCK_NAME = "severity";

    /**
     * Name of the json file.
     */
    protected static final String JSON_FILE_NAME = "severity.json";

    public SeverityPlugin() {
        super(Arrays.asList(
                new SeverityAggregator(), new WidgetAggregator()
        ));
    }

    /**
     * Adds severity to test results.
     */
    private static class SeverityAggregator implements Aggregator {

        @Override
        public void aggregate(final Configuration configuration,
                              final List<LaunchResults> launchesResults,
                              final Path outputDirectory) {
            launchesResults.stream()
                    .flatMap(results -> results.getResults().stream())
                    .forEach(this::setSeverityLevel);

        }

        private void setSeverityLevel(final TestResult result) {
            final SeverityLevel severityLevel = result.findOneLabel(SEVERITY)
                    .flatMap(SeverityLevel::fromValue)
                    .orElse(SeverityLevel.NORMAL);
            result.addExtraBlock(SEVERITY_BLOCK_NAME, severityLevel);
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
        protected List<SeverityData> getData(final List<LaunchResults> launchesResults) {
            return launchesResults.stream()
                    .flatMap(launch -> launch.getResults().stream())
                    .map(this::createData)
                    .collect(Collectors.toList());
        }

        private SeverityData createData(final TestResult result) {
            return new SeverityData()
                    .setUid(result.getUid())
                    .setName(result.getName())
                    .setStatus(result.getStatus())
                    .setTime(result.getTime())
                    .setSeverity(result.getExtraBlock(SEVERITY_BLOCK_NAME));
        }
    }
}
