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
package io.qameta.allure.xray;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.InMemoryReportStorage;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.jira.JiraIssueComment;
import io.qameta.allure.jira.JiraService;
import io.qameta.allure.jira.XrayTestRun;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XrayTestRunExportPluginTest {

    private static final String EXECUTION_ISSUES = "ALLURE-2";
    private static final String TESTRUN_KEY = "ALLURE-1";
    private static final Integer TESTRUN_ID = 1;
    private static final int DEFAULT_PAGE = 1;

    /**
     * Verifies a failed Allure result updates the matching Xray test run to FAIL.
     * The test checks the outgoing execution comment and status update payloads.
     */
    @Description
    @Test
    void shouldExportTestRunToXray() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult testResult = createTestResult(Status.FAILED)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.insecure().next(10))
                .setReportUrl(RandomStringUtils.insecure().next(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mock(JiraService.class);
        when(service.getTestRunsForTestExecution(EXECUTION_ISSUES, DEFAULT_PAGE)).thenReturn(
                Collections.singletonList(new XrayTestRun().setId(TESTRUN_ID).setKey(TESTRUN_KEY).setStatus("TODO"))
        );
        attachXrayInput(EXECUTION_ISSUES, results, executorInfo,
                Collections.singletonList(new XrayTestRun().setId(TESTRUN_ID).setKey(TESTRUN_KEY).setStatus("TODO")));

        final XrayTestRunExportPlugin xrayTestRunExportPlugin = new XrayTestRunExportPlugin(
                true,
                "ALLURE-2",
                Collections.emptyMap(),
                () -> service
        );

        xrayTestRunExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                new InMemoryReportStorage()
        );

        final String reportLink = String.format("[%s|%s]", executorInfo.getBuildName(), executorInfo.getReportUrl());
        assertThat(captureComments(service, 1))
                .extracting(CapturedComment::issue, CapturedComment::body)
                .containsExactly(tuple(EXECUTION_ISSUES, "Execution updated from launch " + reportLink));
        assertThat(captureStatusUpdates(service, 1))
                .extracting(StatusUpdate::id, StatusUpdate::status)
                .containsExactly(tuple(TESTRUN_ID, "FAIL"));
    }

    /**
     * Verifies Xray status precedence chooses FAIL when mixed statuses share a test-run key.
     * The test checks only a FAIL status update is sent for the mixed launch.
     */
    @Description
    @Test
    void shouldExportTestRunToXrayWithAllTypesOfStatues() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult testResultFail = createTestResult(Status.FAILED)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResultFail));

        final TestResult testResultPass = createTestResult(Status.PASSED)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        results.add(testResultPass);

        final TestResult testResultBroken = createTestResult(Status.BROKEN)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        results.add(testResultBroken);

        final TestResult testResultSkipped = createTestResult(Status.SKIPPED)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        results.add(testResultSkipped);

        final TestResult testResultUnknown = createTestResult(Status.UNKNOWN)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        results.add(testResultUnknown);

        final TestResult testResultPass2 = createTestResult(Status.PASSED)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        results.add(testResultPass2);


        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.insecure().next(10))
                .setReportUrl(RandomStringUtils.insecure().next(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mock(JiraService.class);
        when(service.getTestRunsForTestExecution(EXECUTION_ISSUES, DEFAULT_PAGE)).thenReturn(
                Collections.singletonList(new XrayTestRun().setId(TESTRUN_ID).setKey(TESTRUN_KEY).setStatus("TODO"))
        );
        attachXrayInput(EXECUTION_ISSUES, results, executorInfo,
                Collections.singletonList(new XrayTestRun().setId(TESTRUN_ID).setKey(TESTRUN_KEY).setStatus("TODO")));

        final XrayTestRunExportPlugin xrayTestRunExportPlugin = new XrayTestRunExportPlugin(
                true,
                "ALLURE-2",
                Collections.emptyMap(),
                () -> service
        );

        xrayTestRunExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                new InMemoryReportStorage()
        );

        final String reportLink = String.format("[%s|%s]", executorInfo.getBuildName(), executorInfo.getReportUrl());
        assertThat(captureComments(service, 1))
                .extracting(CapturedComment::issue, CapturedComment::body)
                .containsExactly(tuple(EXECUTION_ISSUES, "Execution updated from launch " + reportLink));
        assertThat(captureStatusUpdates(service, 1))
                .extracting(StatusUpdate::id, StatusUpdate::status)
                .containsExactly(tuple(TESTRUN_ID, "FAIL"));
    }

    /**
     * Verifies matching Xray test runs are updated across multiple executions.
     * The test checks all resolved test-run IDs receive PASS and each execution is commented.
     */
    @Description
    @Test
    void shouldUpdateSimilarTestRunsInDifferentExecutions() {
        final String xrayExecutions = "   ALLURE-2,  ALLURE-4,ALLURE-6 ";
        final List<XrayTestRun> testRuns = Arrays.asList(
                new XrayTestRun().setId(0).setKey(TESTRUN_KEY).setStatus("TODO"),
                new XrayTestRun().setId(1).setKey(TESTRUN_KEY).setStatus("TODO"),
                new XrayTestRun().setId(2).setKey(TESTRUN_KEY).setStatus("TODO")
        );

        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult testResult = createTestResult(Status.PASSED)
                .setLinks(Collections.singletonList(new Link().setName(TESTRUN_KEY).setType("tms")));

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.insecure().next(10))
                .setReportUrl(RandomStringUtils.insecure().next(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mock(JiraService.class);
        when(service.getTestRunsForTestExecution("ALLURE-2", DEFAULT_PAGE)).thenReturn(
                Collections.singletonList(testRuns.get(0))
        );
        when(service.getTestRunsForTestExecution("ALLURE-4", DEFAULT_PAGE)).thenReturn(
                Collections.singletonList(testRuns.get(1))
        );
        when(service.getTestRunsForTestExecution("ALLURE-6", DEFAULT_PAGE)).thenReturn(
                Collections.singletonList(testRuns.get(2))
        );
        attachXrayInput(xrayExecutions, results, executorInfo, testRuns);

        final XrayTestRunExportPlugin xrayTestRunExportPlugin = new XrayTestRunExportPlugin(
                true,
                xrayExecutions,
                Collections.emptyMap(),
                () -> service
        );

        xrayTestRunExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                new InMemoryReportStorage()
        );

        final String reportLink = String.format("[%s|%s]", executorInfo.getBuildName(), executorInfo.getReportUrl());
        assertThat(captureComments(service, 3))
                .extracting(CapturedComment::issue, CapturedComment::body)
                .containsExactlyInAnyOrder(
                        tuple("ALLURE-2", "Execution updated from launch " + reportLink),
                        tuple("ALLURE-4", "Execution updated from launch " + reportLink),
                        tuple("ALLURE-6", "Execution updated from launch " + reportLink)
                );
        assertThat(captureStatusUpdates(service, 3))
                .extracting(StatusUpdate::id, StatusUpdate::status)
                .containsExactlyInAnyOrder(
                        tuple(0, "PASS"),
                        tuple(1, "PASS"),
                        tuple(2, "PASS")
                );
    }

    private void attachXrayInput(final String executionIssues,
                                 final Set<TestResult> results,
                                 final ExecutorInfo executorInfo,
                                 final List<XrayTestRun> testRuns) {
        Allure.step("Attach Xray export input", () -> Allure.addAttachment("xray-export-input.txt", "text/plain", String.format(
                "executionIssues=%s%nexecutorBuildName=%s%nexecutorReportUrl=%s%nresults:%n%s%ntestRuns:%n%s",
                executionIssues,
                executorInfo.getBuildName(),
                executorInfo.getReportUrl(),
                describeResults(results),
                describeTestRuns(testRuns)
        )));
    }

    private List<CapturedComment> captureComments(final JiraService service, final int expectedCount) {
        return Allure.step("Capture Xray execution comments", () -> {
            final ArgumentCaptor<String> issueCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<JiraIssueComment> commentCaptor = ArgumentCaptor.forClass(JiraIssueComment.class);
            verify(service, times(expectedCount)).createIssueComment(issueCaptor.capture(), commentCaptor.capture());
            final List<CapturedComment> comments = new ArrayList<>();
            for (int i = 0; i < issueCaptor.getAllValues().size(); i++) {
                comments.add(new CapturedComment(
                        issueCaptor.getAllValues().get(i),
                        commentCaptor.getAllValues().get(i).getBody()
                ));
            }
            Allure.addAttachment("xray-comments.txt", "text/plain", describeComments(comments));
            return comments;
        });
    }

    private List<StatusUpdate> captureStatusUpdates(final JiraService service, final int expectedCount) {
        return Allure.step("Capture Xray status updates", () -> {
            final ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
            final ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
            verify(service, times(expectedCount)).updateTestRunStatus(idCaptor.capture(), statusCaptor.capture());
            final List<StatusUpdate> updates = new ArrayList<>();
            for (int i = 0; i < idCaptor.getAllValues().size(); i++) {
                updates.add(new StatusUpdate(idCaptor.getAllValues().get(i), statusCaptor.getAllValues().get(i)));
            }
            Allure.addAttachment("xray-status-updates.txt", "text/plain", describeStatusUpdates(updates));
            return updates;
        });
    }

    private String describeResults(final Set<TestResult> results) {
        return results.stream()
                .map(result -> String.format(
                        "uid=%s, name=%s, status=%s, links=%s",
                        result.getUid(),
                        result.getName(),
                        result.getStatus(),
                        describeLinks(result.getLinks())
                ))
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeLinks(final List<Link> links) {
        return links.stream()
                .map(link -> link.getType() + ":" + link.getName())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private String describeTestRuns(final List<XrayTestRun> testRuns) {
        return testRuns.stream()
                .map(testRun -> String.format(
                        "id=%s, key=%s, status=%s",
                        testRun.getId(),
                        testRun.getKey(),
                        testRun.getStatus()
                ))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeComments(final List<CapturedComment> comments) {
        return comments.stream()
                .map(comment -> String.format("issue=%s, body=%s", comment.issue, comment.body))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeStatusUpdates(final List<StatusUpdate> updates) {
        return updates.stream()
                .map(update -> String.format("id=%s, status=%s", update.id, update.status))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    static TestResult createTestResult(final Status status) {
        return new TestResult()
                .setUid(RandomStringUtils.insecure().next(10))
                .setName(RandomStringUtils.insecure().next(10))
                .setStatus(status);
    }

    private static final class CapturedComment {
        private final String issue;
        private final String body;

        private CapturedComment(final String issue, final String body) {
            this.issue = issue;
            this.body = body;
        }

        private String issue() {
            return issue;
        }

        private String body() {
            return body;
        }
    }

    private static final class StatusUpdate {
        private final Integer id;
        private final String status;

        private StatusUpdate(final Integer id, final String status) {
            this.id = id;
            this.status = status;
        }

        private Integer id() {
            return id;
        }

        private String status() {
            return status;
        }
    }
}
