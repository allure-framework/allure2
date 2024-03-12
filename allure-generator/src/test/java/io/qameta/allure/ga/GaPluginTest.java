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
package io.qameta.allure.ga;

import io.qameta.allure.DefaultLaunchResults;
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

    @Test
    void shouldGetDefaultExecutor() {
        final String executorType = GaPlugin.getExecutorType(
                List.of(
                        new DefaultLaunchResults(Set.of(), Map.of(), Map.of())
                )
        );

        assertThat(executorType)
                .isEqualTo("local");
    }

    @Test
    void shouldProcessNullExecutor() {
        final String executorType = GaPlugin.getExecutorType(
                List.of(
                        new DefaultLaunchResults(Set.of(), Map.of(), Map.of(
                                ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                                new ExecutorInfo()
                        ))
                )
        );

        assertThat(executorType)
                .isEqualTo("local");
    }

    @Test
    void shouldProcessExecutor() {
        final String executorType = GaPlugin.getExecutorType(
                List.of(
                        new DefaultLaunchResults(Set.of(), Map.of(), Map.of(
                                ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                                new ExecutorInfo()
                                        .setType("some executor type")
                        ))
                )
        );

        assertThat(executorType)
                .isEqualTo("some executor type");
    }
}
