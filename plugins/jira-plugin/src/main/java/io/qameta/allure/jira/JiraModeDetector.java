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

import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.qameta.allure.jira.JiraExportPlugin.checkPluginConfiguration;
import static io.qameta.allure.util.PropertyUtils.getProperty;

public final class JiraModeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraModeDetector.class);

    private static final String MODE_CLOUD = "cloud";
    private static final String MODE_SERVER = "server";

    private static final String JIRA_CLOUD_URL = "ALLURE_JIRA_CLOUD_URL";
    private static final String JIRA_CLOUD_EMAIL = "ALLURE_JIRA_CLOUD_EMAIL";
    private static final String JIRA_CLOUD_API_TOKEN = "ALLURE_JIRA_CLOUD_API_TOKEN";

    private JiraModeDetector() {
    }

    public static String detectMode(final Configuration configuration,
                                    final Supplier<JiraCloudService> cloudServiceSupplier,
                                    final Supplier<JiraService> serverServiceSupplier) {
        if (!isJiraPluginInstalled(configuration)) {
            LOGGER.warn("Jira plugin not found in Allure configuration; defaulting to SERVER mode");
            return MODE_SERVER;
        }

        LOGGER.info("Auto-detecting Jira deployment type...");

        if (hasCloudCredentials()) {
            LOGGER.debug("Cloud credentials detected, trying Cloud API first");
            final Optional<String> cloudMode = tryDetectCloud(cloudServiceSupplier);
            if (cloudMode.isPresent()) {
                return cloudMode.get();
            }
        }

        LOGGER.debug("Trying Server API for detection");
        return tryDetectServer(serverServiceSupplier);
    }

    private static boolean isJiraPluginInstalled(final Configuration configuration) {
        return checkPluginConfiguration(configuration);
    }

    private static boolean hasCloudCredentials() {
        return getProperty(JIRA_CLOUD_URL).isPresent()
                && getProperty(JIRA_CLOUD_EMAIL).isPresent()
                && getProperty(JIRA_CLOUD_API_TOKEN).isPresent();
    }

    private static Optional<String> tryDetectCloud(final Supplier<JiraCloudService> cloudServiceSupplier) {
        try {
            LOGGER.debug("Attempting Cloud API detection via /rest/api/3/myself");
            final JiraCloudService cloudService = cloudServiceSupplier.get();

            if (cloudService == null) {
                LOGGER.debug("Cloud service is null (credentials not configured)");
                return Optional.empty();
            }

            final Response<Void> resp = cloudService.getMyself();
            if (resp != null && resp.isSuccessful()) {
                LOGGER.info("Successfully connected to Jira Cloud");
                return Optional.of(MODE_CLOUD);
            }

            LOGGER.debug("Cloud API detection returned http={}", resp == null ? "null" : resp.code());
            return Optional.empty();

        } catch (Exception e) {
            LOGGER.debug("Cloud API detection failed: {}", e.getMessage());
            return Optional.empty();
        }
    }


    private static String tryDetectServer(final Supplier<JiraService> serverServiceSupplier) {
        try {
            LOGGER.debug("Attempting Server API detection via /rest/api/2/serverInfo");
            final JiraService jiraService = serverServiceSupplier.get();
            final JiraServerInfo serverInfo = jiraService.getServerInfo();

            if (serverInfo == null) {
                LOGGER.warn("Server info is null, defaulting to SERVER mode");
                return MODE_SERVER;
            }

            if (serverInfo.isCloud()) {
                LOGGER.info("Detected Jira Cloud via Server API (deploymentType: Cloud, version: {})",
                        serverInfo.getVersion());
                return MODE_CLOUD;
            }

            LOGGER.info("Detected Jira Server (version: {}, title: {})",
                    serverInfo.getVersion(), serverInfo.getServerTitle());
            return MODE_SERVER;

        } catch (Exception e) {
            LOGGER.warn("Server API detection failed ({}), defaulting to SERVER mode", e.getMessage());
            LOGGER.debug("Detection error details", e);
            return MODE_SERVER;
        }
    }

}
