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
package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Composite aggregator extension. Can be used to process the list of aggregator.
 *
 * @since 2.0
 */
public class CompositeAggregator implements Aggregator {

    private final List<Aggregator> aggregators;

    public CompositeAggregator(final List<Aggregator> aggregators) {
        this.aggregators = aggregators;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        for (Aggregator aggregator : aggregators) {
            aggregator.aggregate(configuration, launchesResults, outputDirectory);
        }
    }
}
