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

import java.util.List;

/**
 * Common json aggregator.
 */
public abstract class CommonJsonAggregator2 implements Aggregator2 {

    private final String location;

    private final String fileName;

    protected CommonJsonAggregator2(final String fileName) {
        this(Constants.DATA_DIR, fileName);
    }

    protected CommonJsonAggregator2(final String location, final String fileName) {
        this.location = location;
        this.fileName = fileName;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final ReportStorage storage) {
        final Object data = getData(launchesResults);
        storage.addDataJson(String.format("%s/%s", this.location, this.fileName), data);
    }

    protected abstract Object getData(List<LaunchResults> launches);
}
