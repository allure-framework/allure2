/*
 *  Copyright 2016-2026 Qameta Software Inc
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
package io.qameta.allure.ga;

import io.qameta.allure.Allure;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.Description;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.executor.ExecutorPlugin;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class GaPluginTest {

    /**
     * Verifies resolving default executor for analytics executor detection.
     */
    @Description
    @Test
    void shouldGetDefaultExecutor() {
        Allure.parameter("executorMetadata", "missing");
        final String executorType = getExecutorType(new DefaultLaunchResults(Set.of(), Map.of(), Map.of()));

        assertThat(executorType)
                .isEqualTo("local");
    }

    /**
     * Verifies processing null executor for analytics executor detection.
     */
    @Description
    @Test
    void shouldProcessNullExecutor() {
        Allure.parameter("executorMetadata", "present without type");
        final String executorType = getExecutorType(
                new DefaultLaunchResults(
                        Set.of(), Map.of(), Map.of(
                                ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                                new ExecutorInfo()
                        )
                )
        );

        assertThat(executorType)
                .isEqualTo("local");
    }

    /**
     * Verifies processing executor for analytics executor detection.
     */
    @Description
    @Test
    void shouldProcessExecutor() {
        Allure.parameter("executorMetadata", "present with type");
        final String executorType = getExecutorType(
                new DefaultLaunchResults(
                        Set.of(), Map.of(), Map.of(
                                ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                                new ExecutorInfo()
                                        .setType("some executor type")
                        )
                )
        );

        assertThat(executorType)
                .isEqualTo("some executor type");
    }

    private String getExecutorType(final DefaultLaunchResults launchResults) {
        return Allure.step(
                "Resolve Google Analytics executor type",
                () -> GaPlugin.getExecutorType(List.of(launchResults))
        );
    }
}
