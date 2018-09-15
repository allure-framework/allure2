package io.qameta.allure.jira;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

/**
 * Jira Service declaration.
 */
public interface JiraService {

    @POST("allure/1.0/launch")
    JiraLaunch createJiraLaunch(@Body JiraLaunch launch);

    @GET("allure/1.0/launch")
    List<JiraLaunch> getJiraLaunches(@Query("issueKey") String issueKey);

    @POST("allure/1.0/testresult")
    JiraTestResult createTestResult(@Body JiraTestResult launch);

    @GET("allure/1.0/testresult")
    List<JiraTestResult> getTestResults(@Query("issueKey") String issueKey);

}
