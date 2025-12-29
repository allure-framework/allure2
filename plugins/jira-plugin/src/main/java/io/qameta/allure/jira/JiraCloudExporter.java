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

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.jira.retrofit.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JiraCloudExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraCloudExporter.class);
    private static final String PROPERTY_KEY = "allure.lastRun";

    private final JiraCloudService jiraService;
    private final String reportUrl;

    public JiraCloudExporter(final JiraCloudService jiraService, final String reportUrl) {
        this.jiraService = Objects.requireNonNull(jiraService, "jiraService cannot be null");
        this.reportUrl = reportUrl;

        if (reportUrl == null || reportUrl.isEmpty()) {
            LOGGER.warn("ALLURE_JIRA_REPORT_URL is not set. Jira issues will not have a link to the Allure report.");
        }
    }

    public void export(final List<LaunchResults> launchResults) {
        LOGGER.info("Starting Jira Cloud export...");

        final Map<String, JiraCloudTestSummary> summaryByIssue = aggregateByIssue(launchResults);

        LOGGER.info("Found {} unique Jira issues with test results", summaryByIssue.size());

        if (summaryByIssue.isEmpty()) {
            LOGGER.info("No Jira issues found in test results (no @Issue annotations or links). Skipping export.");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<String, JiraCloudTestSummary> entry : summaryByIssue.entrySet()) {
            final String issueKey = entry.getKey();
            final JiraCloudTestSummary summary = entry.getValue();

            try {
                exportToIssue(issueKey, summary);
                successCount++;
                LOGGER.info("✓ Successfully exported to issue: {}", issueKey);
            } catch (Exception e) {
                failureCount++;
                LOGGER.warn("✗ Failed to export to issue: {} — {}", issueKey, e.getMessage());
            }
        }

        LOGGER.info("Jira Cloud export completed: {} success, {} failures", successCount, failureCount);
    }

    private Map<String, JiraCloudTestSummary> aggregateByIssue(final List<LaunchResults> launchResults) {
        final Map<String, JiraCloudTestSummary> summaryMap = new HashMap<>();

        launchResults.forEach(launch -> {
            launch.getAllResults().forEach(testResult -> {
                final List<String> issueKeys = extractIssueKeys(testResult);

                for (String issueKey : issueKeys) {
                    summaryMap.computeIfAbsent(issueKey, k -> new JiraCloudTestSummary())
                        .addTestResult(testResult.getStatus());
                }
            });
        });

        summaryMap.values().forEach(summary -> summary.setReportUrl(reportUrl));

        return summaryMap;
    }

    private List<String> extractIssueKeys(final io.qameta.allure.entity.TestResult testResult) {
        return testResult.getLinks().stream()
            .filter(link -> "issue".equalsIgnoreCase(link.getType()))
            .map(io.qameta.allure.entity.Link::getName)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .distinct()
            .collect(Collectors.toList());
    }

    private void exportToIssue(final String issueKey, final JiraCloudTestSummary summary) {
        final retrofit2.Response<Void> resp = jiraService.updateIssueProperty(issueKey, PROPERTY_KEY, summary);

        if (resp == null || !resp.isSuccessful()) {
            final String code = (resp == null) ? "null" : String.valueOf(resp.code());
            throw new ServiceException("Jira Cloud update failed for " + issueKey + ", http=" + code);
        }
    }
}
