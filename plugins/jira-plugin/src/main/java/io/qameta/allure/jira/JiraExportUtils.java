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

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author SeleniumTestAB.
 * <p>
 * Utility Class for Jira Export Plugin
 */

public final class JiraExportUtils {

    private static final String EXECUTORS_BLOCK_NAME = "executor";

    private JiraExportUtils() {
    }


    public static JiraLaunch getJiraLaunch(final ExecutorInfo executor,
                                           final List<LaunchStatisticExport> statistic) {
        return new JiraLaunch()
                .setExternalId(executor.getBuildName())
                .setStatistic(statistic)
                .setName(executor.getBuildName())
                .setUrl(executor.getReportUrl())
                .setDate(System.currentTimeMillis());
    }

    public static Optional<JiraTestResult> getJiraTestResult(final ExecutorInfo executor,
                                                             final TestResult testResult) {
        final List<String> issues = testResult.getLinks().stream()
                .filter(JiraExportUtils::isIssueLink)
                .map(Link::getName)
                .collect(Collectors.toList());
        if (issues.isEmpty()) {
            return Optional.empty();
        } else {
            final JiraTestResult jiraTestResult = new JiraTestResult()
                    .setExternalId(testResult.getUid())
                    .setTestCaseId(testResult.getUid())
                    .setHistoryKey(testResult.getHistoryId())
                    .setName(testResult.getName())
                    .setUrl(getJiraTestResultUrl(executor.getReportUrl(), testResult.getUid()))
                    .setStatus(testResult.getStatus().toString())
                    .setColor(findTestResultsStatusColor(testResult))
                    .setDate(testResult.getTime().getStop())
                    .setLaunchUrl(executor.getReportUrl())
                    .setLaunchName(executor.getBuildName())
                    .setLaunchExternalId(executor.getBuildName());
            return Optional.of(jiraTestResult);
        }
    }


    public static List<TestResult> getTestResults(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static ExecutorInfo getExecutor(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .findFirst()
                .orElse(new ExecutorInfo());
    }

    public static Statistic getStatistic(final List<LaunchResults> launchesResults) {
        final Statistic statistic = new Statistic();
        launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(statistic::update);
        return statistic;
    }

    public static List<LaunchStatisticExport> convertStatistics(final Statistic statistic) {
        return Stream.of(ResultStatus.values()).filter(resultStatus -> statistic.get(resultStatus.statusName()) != 0)
                .map(resultStatus ->
                        new LaunchStatisticExport(resultStatus.statusName().value(),
                                resultStatus.color(), statistic.get(resultStatus.statusName())))
                .collect(Collectors.toList());

    }


    public static String getJiraTestResultUrl(final String reportUrl, final String uuid) {
        return Optional.ofNullable(reportUrl)
                .map(url -> url.endsWith("index.html") ? "%s#testresult/%s" : "%s/#testresult/%s")
                .map(pattern -> String.format(pattern, reportUrl, uuid))
                .orElse(null);
    }

    public static boolean isIssueLink(final Link link) {
        return "issue".equals(link.getType());
    }

    public static List<String> splitByComma(final String value) {
        return Arrays.asList(value.split(","));
    }

    private static String findTestResultsStatusColor(final TestResult testResult) {
        return Stream.of(ResultStatus.values())
                .filter(resultStatus -> testResult.getStatus() == resultStatus.statusName())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("There is no such status as " + testResult.getStatus()
                                .value()))
                .color();
    }
}
