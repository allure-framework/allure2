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
package io.qameta.allure.executor;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.Constants;
import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class ExecutorPlugin extends CommonJsonAggregator implements Reader {

    public static final String EXECUTORS_BLOCK_NAME = "executor";
    protected static final String JSON_FILE_NAME = "executor.json";

    public ExecutorPlugin() {
        super(Constants.WIDGETS_DIR, "executors.json");
    }

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path executorFile = directory.resolve(JSON_FILE_NAME);
        if (Files.exists(executorFile)) {
            try (InputStream is = Files.newInputStream(executorFile)) {
                final ExecutorInfo info = context.getValue().readValue(is, ExecutorInfo.class);
                visitor.visitExtra(EXECUTORS_BLOCK_NAME, info);
            } catch (IOException e) {
                visitor.error("Could not read executor file " + executorFile, e);
            }
        }
    }

    @Override
    protected List<ExecutorInfo> getData(final List<LaunchResults> launches) {
        return launches.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .collect(Collectors.toList());
    }
}
