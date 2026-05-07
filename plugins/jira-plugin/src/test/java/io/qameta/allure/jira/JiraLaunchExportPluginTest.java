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
package io.qameta.allure.jira;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.InMemoryReportStorage;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.jira.TestData.createTestResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JiraLaunchExportPluginTest {

    /**
     * Verifies launch statistics are exported to Jira.
     * The test checks the outgoing launch payload and issue list built from launch results and executor metadata.
     */
    @Description
    @Test
    void shouldExportLaunchToJira() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult passed = createTestResult(Status.PASSED);
        final TestResult failed = createTestResult(Status.FAILED);
        final TestResult broken = createTestResult(Status.BROKEN);
        final TestResult skipped = createTestResult(Status.SKIPPED);
        final TestResult unknown = createTestResult(Status.UNKNOWN);

        final Set<TestResult> results = new HashSet<>(Arrays.asList(passed, failed, broken, skipped, unknown));
        when(launchResults.getAllResults()).thenReturn(results);
        final Statistic statistic = JiraExportUtils.getStatistic(Arrays.asList(launchResults));
        final List<LaunchStatisticExport> launchStatisticExports = JiraExportUtils.convertStatistics(statistic);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.random(10))
                .setReportUrl(RandomStringUtils.random(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));
        final JiraService service = TestData.mockJiraService();
        attachLaunchInput(results, executorInfo, launchStatisticExports);
        final JiraExportPlugin jiraLaunchExportPlugin = new JiraExportPlugin(
                true,
                "ALLURE-1,ALLURE-2",
                () -> service
        );

        jiraLaunchExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                new InMemoryReportStorage()
        );

        final CapturedLaunchExport export = captureLaunchExport(service);

        assertThat(export.launch.getExternalId()).isEqualTo(executorInfo.getBuildName());
        assertThat(export.launch.getUrl()).isEqualTo(executorInfo.getReportUrl());
        assertThat(export.launch.getStatistic()).isEqualTo(launchStatisticExports);
        assertThat(export.launch.getName()).isEqualTo(executorInfo.getBuildName());
        assertThat(export.issues).isNotEmpty();

    }

    private void attachLaunchInput(final Set<TestResult> results,
                                   final ExecutorInfo executorInfo,
                                   final List<LaunchStatisticExport> expectedStatistic) {
        Allure.step(
                "Attach Jira launch input", () -> Allure.addAttachment(
                        "jira-launch-input.txt", "text/plain", String.format(
                                "executorBuildName=%s%nexecutorReportUrl=%s%nresults:%n%s%nexpectedStatistic:%n%s",
                                executorInfo.getBuildName(),
                                executorInfo.getReportUrl(),
                                describeResults(results),
                                describeStatistic(expectedStatistic)
                        )
                )
        );
    }

    private CapturedLaunchExport captureLaunchExport(final JiraService service) {
        return Allure.step("Capture Jira launch export payload", () -> {
            final ArgumentCaptor<JiraLaunch> launchCaptor = ArgumentCaptor.forClass(JiraLaunch.class);
            @SuppressWarnings("unchecked")
            final ArgumentCaptor<List<String>> issuesCaptor = ArgumentCaptor.forClass(List.class);
            verify(service, times(1)).createJiraLaunch(launchCaptor.capture(), issuesCaptor.capture());
            final CapturedLaunchExport export = new CapturedLaunchExport(launchCaptor.getValue(), issuesCaptor.getValue());
            Allure.addAttachment("jira-launch-payload.txt", "text/plain", describeLaunchExport(export));
            return export;
        });
    }

    private String describeResults(final Set<TestResult> results) {
        return results.stream()
                .map(
                        result -> String.format(
                                "uid=%s, name=%s, status=%s",
                                result.getUid(),
                                result.getName(),
                                result.getStatus()
                        )
                )
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeLaunchExport(final CapturedLaunchExport export) {
        return String.format(
                "externalId=%s%nname=%s%nurl=%s%nissues=%s%nstatistic:%n%s",
                export.launch.getExternalId(),
                export.launch.getName(),
                export.launch.getUrl(),
                export.issues,
                describeStatistic(export.launch.getStatistic())
        );
    }

    private String describeStatistic(final List<LaunchStatisticExport> statistic) {
        return statistic.stream()
                .map(
                        item -> String.format(
                                "status=%s, color=%s, count=%s",
                                item.getStatus(),
                                item.getColor(),
                                item.getCount()
                        )
                )
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static final class CapturedLaunchExport {
        private final JiraLaunch launch;
        private final List<String> issues;

        private CapturedLaunchExport(final JiraLaunch launch, final List<String> issues) {
            this.launch = launch;
            this.issues = issues;
        }
    }

}
