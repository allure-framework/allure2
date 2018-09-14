package io.qameta.allure.jira;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

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

public class JiraLaunchExportPluginTest {

    private static final String ISSUE = "ALLURE-1";

    @Rule
    public final EnvironmentVariables jiraEnabled = new EnvironmentVariables()
            .set("ALLURE_JIRA_LAUNCH_ENABLED", "true")
            .set("ALLURE_JIRA_LAUNCH_ISSUE", ISSUE);


    @Test
    public void shouldExportLaunchToJira() {
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult passed = createTestResult(Status.PASSED);
        final TestResult failed = createTestResult(Status.FAILED);
        final TestResult broken = createTestResult(Status.BROKEN);
        final TestResult skipped = createTestResult(Status.SKIPPED);
        final TestResult unknown = createTestResult(Status.UNKNOWN);

        final Set<TestResult> results = new HashSet<>(Arrays.asList(passed, failed, broken, skipped, unknown));
        when(launchResults.getAllResults()).thenReturn(results);

        final ExecutorInfo executorInfo = new ExecutorInfo()
                .setBuildName(RandomStringUtils.random(10))
                .setReportUrl(RandomStringUtils.random(10));
        when(launchResults.getExtra("executor")).thenReturn(Optional.of(executorInfo));

        final JiraService service = mockJiraService();
        final JiraLaunchExportPlugin jiraLaunchExportPlugin = new JiraLaunchExportPlugin(service);

        jiraLaunchExportPlugin.aggregate(
                mock(Configuration.class),
                Collections.singletonList(launchResults),
                Paths.get("/")
        );

        verify(service, times(1)).createJiraLaunch(any(JiraLaunch.class));
        verify(service).createJiraLaunch(argThat(launch -> ISSUE.equals(launch.getIssueKey())));

        verify(service).createJiraLaunch(argThat(launch -> executorInfo.getBuildName().equals(launch.getName())));
        verify(service).createJiraLaunch(argThat(launch -> executorInfo.getReportUrl().equals(launch.getUrl())));

        verify(service).createJiraLaunch(argThat(launch -> launch.getPassed() == 1));
        verify(service).createJiraLaunch(argThat(launch -> launch.getFailed() == 1));
        verify(service).createJiraLaunch(argThat(launch -> launch.getBroken() == 1));
        verify(service).createJiraLaunch(argThat(launch -> launch.getSkipped() == 1));
        verify(service).createJiraLaunch(argThat(launch -> launch.getUnknown() == 1));
    }


}
