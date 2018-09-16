package io.qameta.allure.xray;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.jira.JiraService;
import io.qameta.allure.jira.JiraTestResult;
import io.qameta.allure.jira.XrayTestRun;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class XrayTestRunExportPluginTest {

    private static final String EXECUTION_ISSUES = "ALLURE-2";
    private static final String TESTRUN_KEY = "ALLURE-1";
    private static final Integer TESTRUN_ID = 1;

    @Rule
    public final EnvironmentVariables jiraEnabled = new EnvironmentVariables()
            .set("ALLURE_XRAY_ENABLED", "true")
            .set("ALLURE_XRAY_EXECUTION_ISSUES", EXECUTION_ISSUES);


    @Test
    public void shouldExportTestRunToXray() {
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

        final XrayTestRunExportPlugin xrayTestRunExportPlugin = new XrayTestRunExportPlugin(service);

        xrayTestRunExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );

        final String reportLink = String.format("[%s|%s]", executorInfo.getName(), executorInfo.getReportUrl());
        verify(service, times(1)).createIssueComment(
                argThat(issue -> issue.equals(EXECUTION_ISSUES)),
                argThat(comment -> comment.getBody().contains(reportLink)
        ));
        verify(service, times(1)).updateTestRunStatus(TESTRUN_ID, "FAIL");
    }


    public static TestResult createTestResult(final Status status) {
        return new TestResult()
                .setUid(RandomStringUtils.random(10))
                .setName(RandomStringUtils.random(10))
                .setStatus(status);
    }

}