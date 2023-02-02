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
package io.qameta.allure.jira;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.jira.TestData.ISSUES;
import static io.qameta.allure.jira.TestData.createTestResult;
import static io.qameta.allure.jira.TestData.mockJiraService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JiraTestResultExportPluginTest {


    @Test
    void shouldExportTestResultToJira() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final List<Link> links = ISSUES.stream()
                .map(issue -> new Link().setName(issue).setType("issue"))
                .collect(Collectors.toList());

        final TestResult testResult = createTestResult(Status.PASSED)
                .setLinks(links)
                .setTime(new Time().setStop(90L));

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.random(10))
                .setReportUrl(RandomStringUtils.random(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mockJiraService();
        final JiraExportPlugin jiraExportPlugin = new JiraExportPlugin(
                true,
                "ALLURE-1,ALLURE-2",
                () -> service
        );

        jiraExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );


        verify(service, times(1)).createTestResult(any(JiraTestResult.class), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getExternalId().equals(testResult.getUid())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getTestCaseId().equals(testResult.getUid())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getHistoryKey().equals(testResult.getHistoryId())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getUrl().contains(testResult.getUid())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getName().equals(testResult.getName())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getStatus().equals(testResult.getStatus().toString())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getColor().equals(ResultStatus.PASSED.color())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getDate() == testResult.getTime().getStop().longValue()), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getLaunchUrl().equals(executorInfo.getReportUrl())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getLaunchName().equals(executorInfo.getBuildName())), eq(ISSUES));
        verify(service).createTestResult(argThat(result -> result.getLaunchExternalId().equals(executorInfo.getBuildName())), eq(ISSUES));

    }

}
