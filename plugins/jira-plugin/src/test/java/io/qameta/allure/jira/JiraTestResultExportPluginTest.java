package io.qameta.allure.jira;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.qameta.allure.jira.TestData.createTestResult;
import static io.qameta.allure.jira.TestData.mockJiraService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JiraTestResultExportPluginTest {

    private static final String ISSUE = "ALLURE-1";

    @Rule
    public final EnvironmentVariables jiraEnabled = new EnvironmentVariables()
            .set("ALLURE_JIRA_TESTRESULT_ENABLED", "true");

    @Test
    public void shouldExportTestResultToJira() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult testResult = createTestResult(Status.PASSED)
                .setLinks(Collections.singletonList(new Link().setName(ISSUE).setType("issue")));

        final Set<TestResult> results = new HashSet<>(Arrays.asList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.random(10))
                .setReportUrl(RandomStringUtils.random(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mockJiraService();
        final JiraTestResultExportPlugin jiraTestResultExportPlugin = new JiraTestResultExportPlugin(service);

        jiraTestResultExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );

        verify(service, times(1)).createTestResult(any(JiraTestResult.class));
        verify(service).createTestResult(argThat(result -> ISSUE.equals(result.getIssueKey())));
        verify(service).createTestResult(argThat(result -> testResult.getName().equals(result.getName())));
        verify(service).createTestResult(argThat(result -> testResult.getStatus().equals(result.getStatus())));
        verify(service).createTestResult(argThat(result -> result.getUrl().contains(testResult.getUid())));

        verify(service).createTestResult(argThat(result -> executorInfo.getBuildName().equals(result.getLaunchName())));
        verify(service).createTestResult(argThat(result -> executorInfo.getReportUrl().equals(result.getLaunchUrl())));

    }

}
