package io.qameta.allure.jira;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * Jira Service declaration.
 */
public interface JiraService {

    @POST("api/2/issue/{issueKey}/comment")
    Response<ResponseBody> createIssueComment(@Path("issueKey") String issueKey, @Body JiraIssueComment comment);

    @POST("allure/1.0/launch")
    JiraLaunch createJiraLaunch(@Body JiraLaunch launch);

    @GET("allure/1.0/launch")
    List<JiraLaunch> getJiraLaunches(@Query("issueKey") String issueKey);

    @POST("allure/1.0/testresult")
    JiraTestResult createTestResult(@Body JiraTestResult launch);

    @GET("allure/1.0/testresult")
    List<JiraTestResult> getTestResults(@Query("issueKey") String issueKey);

    @GET("raven/1.0/api/testexec/{issueKey}/test")
    List<XrayTestRun> getTestRunsForTestExecution(@Path("issueKey") String issueKey);

    @PUT("raven/1.0/api/testrun/{id}/status")
    Response<ResponseBody> updateTestRunStatus(@Path("id") Integer id, @Query("status") String status);

}
