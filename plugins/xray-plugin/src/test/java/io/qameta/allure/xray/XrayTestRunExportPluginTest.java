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
package io.qameta.allure.xray;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.jira.JiraService;
import io.qameta.allure.jira.XrayTestRun;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XrayTestRunExportPluginTest {

    private static final String EXECUTION_ISSUES = "ALLURE-2";
    private static final String TESTRUN_KEY = "ALLURE-1";
    private static final Integer TESTRUN_ID = 1;

    @Test
    void shouldExportTestRunToXray() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult testResult = createTestResult(Status.FAILED)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.random(10))
                .setReportUrl(RandomStringUtils.random(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mock(JiraService.class);
        when(service.getTestRunsForTestExecution(EXECUTION_ISSUES)).thenReturn(
                Collections.singletonList(new XrayTestRun().setId(TESTRUN_ID).setKey(TESTRUN_KEY).setStatus("TODO"))
        );

        final XrayTestRunExportPlugin xrayTestRunExportPlugin = new XrayTestRunExportPlugin(
                true,
                "ALLURE-2",
                Collections.emptyMap(),
                () -> service
        );

        xrayTestRunExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );

        final String reportLink = String.format("[%s|%s]", executorInfo.getBuildName(), executorInfo.getReportUrl());
        verify(service, times(1)).createIssueComment(
                argThat(issue -> issue.equals(EXECUTION_ISSUES)),
                argThat(comment -> comment.getBody().contains(reportLink)
                ));
        verify(service, times(1)).updateTestRunStatus(TESTRUN_ID, "FAIL");
    }


    static TestResult createTestResult(final Status status) {
        return new TestResult()
                .setUid(RandomStringUtils.random(10))
                .setName(RandomStringUtils.random(10))
                .setStatus(status);
    }

}
