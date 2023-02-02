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

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.qameta.allure.util.PropertyUtils.getProperty;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */


public class JiraExportPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraExportPlugin.class);

    private static final String ALLURE_JIRA_ENABLED = "ALLURE_JIRA_ENABLED";
    private static final String ALLURE_JIRA_LAUNCH_ISSUES = "ALLURE_JIRA_LAUNCH_ISSUES";

    private final Supplier<JiraService> jiraServiceSupplier;
    private final boolean enabled;
    private final String issues;

    public JiraExportPlugin() {
        this(
                getProperty(ALLURE_JIRA_ENABLED).map(Boolean::parseBoolean).orElse(false),
                getProperty(ALLURE_JIRA_LAUNCH_ISSUES).orElse(""),
                () -> new JiraServiceBuilder().defaults().build()
        );
    }

    public JiraExportPlugin(final boolean enabled,
                            final String issues,
                            final Supplier<JiraService> jiraServiceSupplier) {
        this.jiraServiceSupplier = jiraServiceSupplier;
        this.enabled = enabled;
        this.issues = issues;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        if (enabled) {
            final JiraService jiraService = jiraServiceSupplier.get();

            final List<String> issues = JiraExportUtils.splitByComma(this.issues);
            final ExecutorInfo executor = JiraExportUtils.getExecutor(launchesResults);
            final Statistic statisticToConvert = JiraExportUtils.getStatistic(launchesResults);
            final List<LaunchStatisticExport> statistic = JiraExportUtils.convertStatistics(statisticToConvert);
            final JiraLaunch launch = JiraExportUtils.getJiraLaunch(executor, statistic);
            exportLaunchToJira(jiraService, launch, issues);

            JiraExportUtils.getTestResults(launchesResults).stream()
                    .map(testResult -> JiraExportUtils.getJiraTestResult(executor, testResult))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(jiraTestResult -> {
                        JiraExportUtils.getTestResults(launchesResults)
                                .stream()
                                .forEach(testResult ->
                                        exportTestResultToJira(jiraService, jiraTestResult, testResult));
                    });
        }
    }


    private List<JiraExportResult> exportLaunchToJira(final JiraService jiraService,
                                                      final JiraLaunch launch,
                                                      final List<String> issues) {
        try {
            final List<JiraExportResult> created = jiraService.createJiraLaunch(launch, issues);

            final List<JiraExportResult> failedExports = findFailuresInExportResult(created);

            if (!failedExports.isEmpty()) {
                logErrorResults(failedExports);
            } else {
                LOGGER.info(String.format("Allure launch '%s' synced with issues  successfully%n",
                        issues));
                LOGGER.info(String.format("Results of launch export %n %s", created));
            }

            return created;
        } catch (Throwable e) {
            LOGGER.error(String.format("Allure launch sync with issue '%s' error", issues), e);
            throw e;
        }
    }

    private void exportTestResultToJira(final JiraService jiraService,
                                        final JiraTestResult jiraTestResult,
                                        final TestResult testResult) {

        if (!Objects.equals(jiraTestResult.getTestCaseId(), testResult.getUid())) {
            return;
        }

        try {
            final List<String> issues = testResult.getLinks().stream()
                    .filter(JiraExportUtils::isIssueLink)
                    .map(Link::getName)
                    .collect(Collectors.toList());

            final List<JiraExportResult> created = jiraService.createTestResult(jiraTestResult, issues);
            final List<JiraExportResult> failedExports = findFailuresInExportResult(created);

            if (!failedExports.isEmpty()) {
                logErrorResults(failedExports);
            } else {
                LOGGER.info("All Test Results have been successfully exported to Jira");
            }

        } catch (Throwable e) {
            LOGGER.error(String.format("Allure test result sync with issue '%s' failed",
                    jiraTestResult.getExternalId()), e);
            throw e;
        }
    }

    private void logErrorResults(final List<JiraExportResult> failedExportResults) {
        LOGGER.error(String.format("There was an failure in response%n %s", failedExportResults));
    }


    private List<JiraExportResult> findFailuresInExportResult(final List<JiraExportResult> exportResults) {
        return exportResults.stream()
                .filter(exportResult -> exportResult.getStatus().equals(Status.FAILED.value()))
                .collect(Collectors.toList());
    }

}
