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
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.jira.TestData.ISSUES;
import static io.qameta.allure.jira.TestData.createTestResult;
import static io.qameta.allure.jira.TestData.mockJiraService;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JiraTestResultExportPluginTest {

    /**
     * Verifies individual Allure test results are exported to Jira.
     * The test checks the outgoing Jira test-result payload and linked issue list.
     */
    @Description
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
        attachTestResultInput(testResult, executorInfo);

        jiraExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                new InMemoryReportStorage()
        );


        final JiraTestResult exported = captureTestResultExport(service);

        assertThat(exported.getExternalId()).isEqualTo(testResult.getUid());
        assertThat(exported.getTestCaseId()).isEqualTo(testResult.getUid());
        assertThat(exported.getHistoryKey()).isEqualTo(testResult.getHistoryId());
        assertThat(exported.getUrl()).contains(testResult.getUid());
        assertThat(exported.getName()).isEqualTo(testResult.getName());
        assertThat(exported.getStatus()).isEqualTo(testResult.getStatus().toString());
        assertThat(exported.getColor()).isEqualTo(ResultStatus.PASSED.color());
        assertThat(exported.getDate()).isEqualTo(testResult.getTime().getStop());
        assertThat(exported.getLaunchUrl()).isEqualTo(executorInfo.getReportUrl());
        assertThat(exported.getLaunchName()).isEqualTo(executorInfo.getBuildName());
        assertThat(exported.getLaunchExternalId()).isEqualTo(executorInfo.getBuildName());

    }

    private void attachTestResultInput(final TestResult testResult, final ExecutorInfo executorInfo) {
        Allure.step("Attach Jira test-result input", () -> Allure.addAttachment(
                "jira-test-result-input.txt",
                "text/plain",
                String.format(
                        "uid=%s%nname=%s%nhistoryId=%s%nstatus=%s%nstop=%s%nlinks=%s%n"
                                + "executorBuildName=%s%nexecutorReportUrl=%s",
                        testResult.getUid(),
                        testResult.getName(),
                        testResult.getHistoryId(),
                        testResult.getStatus(),
                        testResult.getTime().getStop(),
                        describeLinks(testResult.getLinks()),
                        executorInfo.getBuildName(),
                        executorInfo.getReportUrl()
                )
        ));
    }

    private JiraTestResult captureTestResultExport(final JiraService service) {
        return Allure.step("Capture Jira test-result export payload", () -> {
            final ArgumentCaptor<JiraTestResult> resultCaptor = ArgumentCaptor.forClass(JiraTestResult.class);
            verify(service, times(1)).createTestResult(resultCaptor.capture(), eq(ISSUES));
            final JiraTestResult result = resultCaptor.getValue();
            Allure.addAttachment("jira-test-result-payload.txt", "text/plain", describeTestResultExport(result));
            return result;
        });
    }

    private String describeLinks(final List<Link> links) {
        return links.stream()
                .map(link -> link.getType() + ":" + link.getName())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private String describeTestResultExport(final JiraTestResult result) {
        return String.format(
                "externalId=%s%ntestCaseId=%s%nhistoryKey=%s%nname=%s%nurl=%s%nstatus=%s%ncolor=%s%n"
                        + "date=%s%nlaunchExternalId=%s%nlaunchName=%s%nlaunchUrl=%s",
                result.getExternalId(),
                result.getTestCaseId(),
                result.getHistoryKey(),
                result.getName(),
                result.getUrl(),
                result.getStatus(),
                result.getColor(),
                result.getDate(),
                result.getLaunchExternalId(),
                result.getLaunchName(),
                result.getLaunchUrl()
        );
    }
}
