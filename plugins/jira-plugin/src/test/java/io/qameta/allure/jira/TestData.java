package io.qameta.allure.jira;

import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestData {

    private TestData(){
    }

    public static JiraService mockJiraService() {
        final JiraService service = mock(JiraService.class);
        when(service.createJiraLaunch(any(JiraLaunch.class))).thenAnswer(i -> {
            final JiraLaunch launch = i.getArgument(0);
            launch.setId(RandomUtils.nextInt());
            return launch;
        });
        when(service.createTestResult(any(JiraTestResult.class))).thenAnswer(i -> {
            final JiraTestResult testResult = i.getArgument(0);
            testResult.setId(RandomUtils.nextInt());
            return testResult;
        });
        return service;
    }

    public static TestResult createTestResult(final Status status) {
        return new TestResult()
                .setUid(RandomStringUtils.random(10))
                .setName(RandomStringUtils.random(10))
                .setStatus(status);
    }

}
