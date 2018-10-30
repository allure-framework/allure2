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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.jira.TestData.createTestResult;
import static io.qameta.allure.jira.TestData.mockJiraService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JiraTestResultExportPluginTest {

    private static final List<String> ISSUES = Arrays.asList("ALLURE-1", "ALLURE_2");

    @Rule
    public final EnvironmentVariables jiraEnabled = new EnvironmentVariables()
            .set("ALLURE_JIRA_ENABLED", "true");

    @Test
    public void shouldExportTestResultToJira() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final List<Link> links = ISSUES.stream()
                .map(issue -> new Link().setName(issue).setType("issue"))
                .collect(Collectors.toList());

        final TestResult testResult = createTestResult(Status.PASSED)
                .setLinks(links);

        final Set<TestResult> results = new HashSet<>(Collections.singletonList(testResult));
        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.random(10))
                .setReportUrl(RandomStringUtils.random(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mockJiraService();
        final JiraExportPlugin jiraExportPlugin = new JiraExportPlugin(() -> service);

        jiraExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );
        verify(service, times(1)).createJiraLaunch(any(JiraLaunch.class));
        verify(service).createJiraLaunch(argThat(launch -> executorInfo.getBuildName().equals(launch.getName())));
        verify(service).createJiraLaunch(argThat(launch -> executorInfo.getReportUrl().equals(launch.getUrl())));

        verify(service, times(1)).createTestResult(any(JiraTestResult.class));
        verify(service).createTestResult(argThat(result -> result.getIssueKeys().size() == 2));
        verify(service).createTestResult(argThat(result -> testResult.getName().equals(result.getName())));
        verify(service).createTestResult(argThat(result -> testResult.getStatus().toString().equals(result.getStatus())));
        verify(service).createTestResult(argThat(result -> result.getUrl().contains(testResult.getUid())));
        verify(service).createTestResult(argThat(result -> Objects.nonNull(result.getLaunchId())));

    }

}
