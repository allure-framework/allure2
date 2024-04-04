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
package io.qameta.allure.summary;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DefaultLaunchResults;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.executor.ExecutorPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author charlie (Dmitry Baev).
 */
class SummaryPluginTest {

    @Test
    void shouldGetReportNameFromConfiguration() {
        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(Set.of(), Map.of(), Map.of())
        );
        final String reportName = "some report name";

        final Configuration configuration = ConfigurationBuilder.empty()
                .withReportName(reportName)
                .build();

        final ReportStorage storage = mock();
        new SummaryPlugin().aggregate(configuration, launchResults, storage);

        final ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.captor();

        verify(storage, times(1))
                .addDataJson(eq("widgets/summary.json"), dataCaptor.capture());

        assertThat(dataCaptor.getValue())
                .isInstanceOf(SummaryData.class);

        final SummaryData data = (SummaryData) dataCaptor.getValue();

        assertThat(data.getReportName())
                .isEqualTo(reportName);
    }

    @Test
    void shouldGetReportNameFromExecutorJson() {
        final String reportName = "other report name";

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(
                        Set.of(),
                        Map.of(),
                        Map.of(
                                ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                                new ExecutorInfo()
                                        .setReportName(reportName)
                        )
                )
        );

        final Configuration configuration = ConfigurationBuilder.empty()
                .withReportName(reportName)
                .build();

        final ReportStorage storage = mock();
        new SummaryPlugin().aggregate(configuration, launchResults, storage);

        final ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.captor();

        verify(storage, times(1))
                .addDataJson(eq("widgets/summary.json"), dataCaptor.capture());

        assertThat(dataCaptor.getValue())
                .isInstanceOf(SummaryData.class);

        final SummaryData data = (SummaryData) dataCaptor.getValue();

        assertThat(data.getReportName())
                .isEqualTo(reportName);
    }

    @Test
    void shouldReportNameFromConfigurationShouldOverride() {
        final String reportName = "other report name";

        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(
                        Set.of(),
                        Map.of(),
                        Map.of(
                                ExecutorPlugin.EXECUTORS_BLOCK_NAME,
                                new ExecutorInfo()
                                        .setReportName("report name from executors")
                        )
                )
        );

        final Configuration configuration = ConfigurationBuilder.empty()
                .withReportName(reportName)
                .build();

        final ReportStorage storage = mock();
        new SummaryPlugin().aggregate(configuration, launchResults, storage);

        final ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.captor();

        verify(storage, times(1))
                .addDataJson(eq("widgets/summary.json"), dataCaptor.capture());

        assertThat(dataCaptor.getValue())
                .isInstanceOf(SummaryData.class);

        final SummaryData data = (SummaryData) dataCaptor.getValue();

        assertThat(data.getReportName())
                .isEqualTo(reportName);
    }

    @Test
    void shouldUseDefaultReportNameIfNotSpecified() {
        final List<LaunchResults> launchResults = List.of(
                new DefaultLaunchResults(
                        Set.of(),
                        Map.of(),
                        Map.of()
                )
        );

        final Configuration configuration = ConfigurationBuilder.empty()
                .build();

        final ReportStorage storage = mock();
        new SummaryPlugin().aggregate(configuration, launchResults, storage);

        final ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.captor();

        verify(storage, times(1))
                .addDataJson(eq("widgets/summary.json"), dataCaptor.capture());

        assertThat(dataCaptor.getValue())
                .isInstanceOf(SummaryData.class);

        final SummaryData data = (SummaryData) dataCaptor.getValue();

        assertThat(data.getReportName())
                .isEqualTo("Allure Report");
    }
}
