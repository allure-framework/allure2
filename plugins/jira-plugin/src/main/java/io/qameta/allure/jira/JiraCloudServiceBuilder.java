/*
 *  Copyright 2016-2024 Qameta Software Inc
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.jira.retrofit.BasicAuthInterceptor;
import io.qameta.allure.jira.retrofit.DefaultCallAdapterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.qameta.allure.util.PropertyUtils.requireProperty;

public class JiraCloudServiceBuilder {

    private static final String JIRA_CLOUD_URL = "ALLURE_JIRA_CLOUD_URL";
    private static final String JIRA_CLOUD_EMAIL = "ALLURE_JIRA_CLOUD_EMAIL";
    private static final String JIRA_CLOUD_API_TOKEN = "ALLURE_JIRA_CLOUD_API_TOKEN";

    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_WRITE_TIMEOUT_SECONDS = 10;

    private String endpoint;
    private String email;
    private String apiToken;

    public JiraCloudServiceBuilder endpoint(final String endpoint) {
        Objects.requireNonNull(endpoint, "endpoint cannot be null");
        this.endpoint = addSlashIfMissing(endpoint);
        return this;
    }

    public JiraCloudServiceBuilder email(final String email) {
        Objects.requireNonNull(email, "email cannot be null");
        this.email = email;
        return this;
    }

    public JiraCloudServiceBuilder apiToken(final String apiToken) {
        Objects.requireNonNull(apiToken, "apiToken cannot be null");
        this.apiToken = apiToken;
        return this;
    }

    public JiraCloudServiceBuilder defaults() {
        endpoint(requireProperty(JIRA_CLOUD_URL));
        email(requireProperty(JIRA_CLOUD_EMAIL));
        apiToken(requireProperty(JIRA_CLOUD_API_TOKEN));
        return this;
    }

    public JiraCloudService build() {
        final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BasicAuthInterceptor(email, apiToken))
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

        final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(endpoint)
            .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()))
            .addCallAdapterFactory(new DefaultCallAdapterFactory<>())
            .client(client)
            .build();

        return retrofit.create(JiraCloudService.class);
    }

    private static String addSlashIfMissing(final String endpoint) {
        final String slash = "/";
        return endpoint.endsWith(slash) ? endpoint : endpoint + slash;
    }
}
