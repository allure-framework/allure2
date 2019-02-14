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
package io.qameta.allure.xray;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.jira.JiraIssueComment;
import io.qameta.allure.jira.JiraService;
import io.qameta.allure.jira.JiraServiceBuilder;
import io.qameta.allure.jira.XrayTestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.qameta.allure.util.PropertyUtils.getProperty;

/**
 * Plugin update Xray test run status from test result.
 */
public class XrayTestRunExportPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(XrayTestRunExportPlugin.class);

    private static final String EXECUTORS_BLOCK_NAME = "executor";

    private static final String ALLURE_XRAY_ENABLED = "ALLURE_XRAY_ENABLED";
    private static final String ALLURE_XRAY_EXECUTION_ISSUES = "ALLURE_XRAY_EXECUTION_ISSUES";

    private static final String ALLURE_XRAY_STATUS_UNKNOWN = "ALLURE_XRAY_STATUS_UNKNOWN";
    private static final String ALLURE_XRAY_STATUS_SKIPPED = "ALLURE_XRAY_STATUS_SKIPPED";
    private static final String ALLURE_XRAY_STATUS_BROKEN = "ALLURE_XRAY_STATUS_BROKEN";
    private static final String ALLURE_XRAY_STATUS_FAILED = "ALLURE_XRAY_STATUS_FAILED";
    private static final String ALLURE_XRAY_STATUS_PASSED = "ALLURE_XRAY_STATUS_PASSED";

    private static final String XRAY_STATUS_PASS = "PASS";
    private static final String XRAY_STATUS_FAIL = "FAIL";
    private static final String XRAY_STATUS_TODO = "TODO";

    private final boolean enabled;
    private final String issues;
    private final Map<Status, String> statusesMap = getDefaultStatusesMap();
    private final Supplier<JiraService> jiraServiceSupplier;

    public XrayTestRunExportPlugin() {
        this(
                getProperty(ALLURE_XRAY_ENABLED).map(Boolean::parseBoolean).orElse(false),
                getProperty(ALLURE_XRAY_EXECUTION_ISSUES).orElse(""),
                getEnvStatusesMap(),
                () -> new JiraServiceBuilder().defaults().build()
        );
    }

    public XrayTestRunExportPlugin(final boolean enabled,
                                   final String issues,
                                   final Map<Status, String> statusesMap,
                                   final Supplier<JiraService> jiraServiceSupplier) {
        this.enabled = enabled;
        this.issues = issues;
        this.statusesMap.putAll(statusesMap);
        this.jiraServiceSupplier = jiraServiceSupplier;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        if (enabled) {
            updateTestRunStatuses(launchesResults);
        }
    }

    private void updateTestRunStatuses(final List<LaunchResults> launchesResults) {
        final List<String> executionIssues = splitByComma(issues);
        final JiraService jiraService = jiraServiceSupplier.get();

        final Map<String, XrayTestRun> testRunsMap = executionIssues.stream()
                .map(jiraService::getTestRunsForTestExecution)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(XrayTestRun::getKey, r -> r));

        launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(testResult -> updateTestRunStatuses(jiraService, testResult, statusesMap, testRunsMap));

        final Optional<ExecutorInfo> executorInfo = launchesResults.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .findFirst();
        executorInfo.ifPresent(info -> executionIssues.forEach(issue -> addExecutionComment(jiraService, issue, info)));
    }

    private void updateTestRunStatuses(final JiraService jiraService,
                                       final TestResult testResult,
                                       final Map<Status, String> statusesMap,
                                       final Map<String, XrayTestRun> testRunsMap) {
        final String status = statusesMap.get(testResult.getStatus());
        final List<XrayTestRun> testRunIssueKeys = testResult.getLinks().stream()
                .filter(this::isTmsLink)
                .map(Link::getName)
                .map(testRunsMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        testRunIssueKeys.forEach(testRun -> updateTestRunStatus(jiraService, testRun, status));
    }

    private void addExecutionComment(final JiraService jiraService,
                                     final String executionIssueKey,
                                     final ExecutorInfo info) {
        try {
            final String message = String.format("Execution updated from launch [%s|%s]",
                    info.getBuildName(), info.getReportUrl());
            jiraService.createIssueComment(executionIssueKey, new JiraIssueComment().setBody(message));
            LOGGER.debug(String.format("Xray execution '%s' commented successfully", executionIssueKey));
        } catch (Exception e) {
            LOGGER.debug(String.format("Xray execution '%s' comment failed", executionIssueKey));
        }
    }

    private void updateTestRunStatus(final JiraService jiraService,
                                     final XrayTestRun testRun,
                                     final String status) {
        if (!status.equals(testRun.getStatus())) {
            try {
                jiraService.updateTestRunStatus(testRun.getId(), status);
                LOGGER.debug(String.format("Xray testrun '%s' status updated to '%s' successfully",
                        testRun.getKey(), status));
            } catch (Exception e) {
                LOGGER.error(String.format("Xray testrun '%s' status update failed",
                        testRun.getKey()));
            }
        }
    }

    private static Map<Status, String> getEnvStatusesMap() {
        final Map<Status, String> statues = new HashMap<>();
        getProperty(ALLURE_XRAY_STATUS_PASSED).ifPresent(value -> statues.put(Status.PASSED, value));
        getProperty(ALLURE_XRAY_STATUS_FAILED).ifPresent(value -> statues.put(Status.FAILED, value));
        getProperty(ALLURE_XRAY_STATUS_BROKEN).ifPresent(value -> statues.put(Status.BROKEN, value));
        getProperty(ALLURE_XRAY_STATUS_SKIPPED).ifPresent(value -> statues.put(Status.SKIPPED, value));
        getProperty(ALLURE_XRAY_STATUS_UNKNOWN).ifPresent(value -> statues.put(Status.UNKNOWN, value));
        return statues;
    }

    private static Map<Status, String> getDefaultStatusesMap() {
        final Map<Status, String> statues = new HashMap<>();
        statues.put(Status.PASSED, XRAY_STATUS_PASS);
        statues.put(Status.FAILED, XRAY_STATUS_FAIL);
        statues.put(Status.BROKEN, XRAY_STATUS_FAIL);
        statues.put(Status.SKIPPED, XRAY_STATUS_TODO);
        statues.put(Status.UNKNOWN, XRAY_STATUS_TODO);
        return statues;
    }

    private boolean isTmsLink(final Link link) {
        return "tms".equals(link.getType());
    }

    private static List<String> splitByComma(final String value) {
        return Arrays.asList(value.split(","));
    }
}
