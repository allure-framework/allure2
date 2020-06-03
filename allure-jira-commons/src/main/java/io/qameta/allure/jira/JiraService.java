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
    List<JiraExportResult> createJiraLaunch(@Body JiraLaunch launch, @Query("issueKey") List<String> issueKey);

    @GET("allure/1.0/launch")
    List<JiraLaunch> getJiraLaunches(@Query("issueKey") String issueKey);

    @POST("allure/1.0/testresult")
    List<JiraExportResult> createTestResult(@Body JiraTestResult launch, @Query("issueKey") List<String> issueKey);

    @GET("allure/1.0/testresult")
    List<JiraTestResult> getTestResults(@Query("issueKey") String issueKey);

    @GET("raven/1.0/api/testexec/{issueKey}/test")
    List<XrayTestRun> getTestRunsForTestExecution(@Path("issueKey") String issueKey);

    @PUT("raven/1.0/api/testrun/{externalId}/status")
    Response<ResponseBody> updateTestRunStatus(@Path("externalId") Integer externalId, @Query("status") String status);

}
