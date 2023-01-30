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
package io.qameta.allure.owner;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;

import java.nio.file.Path;
import java.util.List;

/**
 * Plugin that adds owner information to test results.
 *
 * @since 2.0
 */
public class OwnerPlugin implements Aggregator {

    public static final String OWNER_BLOCK_NAME = "owner";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .forEach(this::setOwner);
    }

    private void setOwner(final TestResult result) {
        result.findOneLabel(LabelName.OWNER)
                .ifPresent(owner -> result.addExtraBlock(OWNER_BLOCK_NAME, owner));
    }
}
