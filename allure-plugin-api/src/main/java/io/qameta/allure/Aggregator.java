/*
 *  Copyright 2019 Qameta Software OÜ
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
import java.util.Optional;

/**
 * Aggregator extension. Can be used to process results and/or generate
 * some data to report directory.
 *
 * @since 2.0
 */
@FunctionalInterface
public interface Aggregator extends Extension {

    /**
     * Configure the number of items to be rendered on different charts.
     *
     * @return RESULTS_LIMIT provided by user, or a default value = 20.
     */
    static long resultsLimit() {
        return Optional.ofNullable(System.getenv("ALLURE_RESULTS_LIMIT"))
                .map(Long::parseLong)
                .orElse(20L);
    }

    /**
     * Process report data.
     *
     * @param configuration   the report configuration.
     * @param launchesResults all the parsed test results.
     * @param outputDirectory the report directory.
     * @throws IOException if any occurs.
     */
    void aggregate(Configuration configuration,
                   List<LaunchResults> launchesResults,
                   Path outputDirectory) throws IOException;

}
