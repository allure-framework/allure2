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
package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.util.List;

/**
 * Composite aggregator extension. Can be used to process the list of aggregator.
 *
 * @since 2.0
 */
public class CompositeAggregator2 implements Aggregator2 {

    private final List<Aggregator2> aggregators;

    public CompositeAggregator2(final List<Aggregator2> aggregators) {
        this.aggregators = aggregators;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        for (Aggregator2 aggregator : aggregators) {
            aggregator.aggregate(configuration, launchesResults, storage);
        }
    }
}
