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
package io.qameta.allure.jira;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


import static io.qameta.allure.jira.TestData.createTestResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class JiraLaunchExportPluginTest {


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
        final JiraExportPlugin jiraLaunchExportPlugin = new JiraExportPlugin(
                true,
                "ALLURE-1,ALLURE-2",
                () -> service
        );

        jiraLaunchExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );

        verify(service, times(1)).createJiraLaunch(any(JiraLaunch.class), anyList());

        verify(service).createJiraLaunch(argThat(launch -> executorInfo.getBuildName().equals(launch.getExternalId())), anyList());
        verify(service).createJiraLaunch(argThat(launch -> executorInfo.getReportUrl().equals(launch.getUrl())), anyList());
        verify(service).createJiraLaunch(argThat(launch -> launchStatisticExports.equals(launch.getStatistic())), anyList());
        verify(service).createJiraLaunch(argThat(launch -> executorInfo.getBuildName().equals(launch.getName())), anyList());
        verify(service).createJiraLaunch(any(JiraLaunch.class), argThat(issues -> !issues.isEmpty()));

    }


}
