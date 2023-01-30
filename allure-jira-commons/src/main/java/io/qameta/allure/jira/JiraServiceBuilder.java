/*
 *  Copyright 2016-2023 Qameta Software OÜ
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

import io.qameta.allure.jira.retrofit.BasicAuthInterceptor;
import io.qameta.allure.jira.retrofit.DefaultCallAdapterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Objects;

import static io.qameta.allure.util.PropertyUtils.requireProperty;

/**
 * Jira Service builder.
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class JiraServiceBuilder {

    private static final String JIRA_ENDPOINT = "ALLURE_JIRA_ENDPOINT";
    private static final String JIRA_USERNAME = "ALLURE_JIRA_USERNAME";
    private static final String JIRA_PASSWORD = "ALLURE_JIRA_PASSWORD";

    private String endpoint;
    private String username;
    private String password;

    public JiraServiceBuilder endpoint(final String endpoint) {
        Objects.requireNonNull(endpoint);
        this.endpoint = addSlashIfMissing(endpoint);
        return this;
    }

    public JiraServiceBuilder username(final String username) {
        Objects.requireNonNull(username);
        this.username = username;
        return this;
    }

    public JiraServiceBuilder password(final String password) {
        Objects.requireNonNull(password);
        this.password = password;
        return this;
    }

    public JiraServiceBuilder defaults() {
        endpoint(requireProperty(JIRA_ENDPOINT));
        username(requireProperty(JIRA_USERNAME));
        password(requireProperty(JIRA_PASSWORD));
        return this;
    }

    public JiraService build() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username, password))
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(new DefaultCallAdapterFactory<>())
                .client(client)
                .build();
        return retrofit.create(JiraService.class);
    }

    private static String addSlashIfMissing(final String endpoint) {
        final String slash = "/";
        return endpoint.endsWith(slash) ? endpoint : endpoint + slash;
    }

}
