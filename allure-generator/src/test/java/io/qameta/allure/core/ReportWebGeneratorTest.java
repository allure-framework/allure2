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

import io.qameta.allure.DefaultConfiguration;
import io.qameta.allure.context.FreemarkerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
class ReportWebGeneratorTest {

    @SetEnvironmentVariable(key = "ALLURE_NO_ANALYTICS", value = "true")
    @Test
    void shouldDisableAnalytics(@TempDir final Path tempDirectory) {
        final DefaultConfiguration configuration = new DefaultConfiguration(Collections.singletonList(new FreemarkerContext()), Collections.emptyList());
        final InMemoryReportStorage reportStorage = new InMemoryReportStorage();
        new ReportWebGenerator()
                .generate(
                        configuration,
                        reportStorage,
                        tempDirectory
                );

        final Path indexHtml = tempDirectory.resolve("index.html");

        assertThat(indexHtml)
                .isRegularFile()
                .content(StandardCharsets.UTF_8)
                .doesNotContain("googletagmanager");
    }
}
