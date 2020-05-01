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
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public final class JiraExportUtility {

    private static final String EXECUTORS_BLOCK_NAME = "executor";
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraExportPlugin.class);

    private JiraExportUtility() {
    }


    public static void handleFailedExport(final List<JiraExportResult> exportResults) {
        final Optional<JiraExportResult> failedResult = exportResults.stream()
                .filter(exportResult -> exportResult.getStatus().equals(Status.FAILED.value()))
                .findFirst();
        if (failedResult.isPresent()) {
            LOGGER.error(String.format("There was an failure in response%n %s", failedResult.get()));
        }

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
                .filter(JiraExportUtility::isIssueLink)
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
                    .setColor(findColorForStatus(testResult.getStatus()))
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
        return Stream.of(Status.values()).filter(status -> statistic.get(status) != 0)
                .map(status ->
                        new LaunchStatisticExport(status.value(),
                                findColorForStatus(status), statistic.get(status)))
                .collect(Collectors.toList());

    }

    public static String findColorForStatus(final Status status) {
        switch (status) {
            case FAILED:
                return StatusColor.RED.value();
            case PASSED:
                return StatusColor.GREEN.value();
            case SKIPPED:
                return StatusColor.GRAY.value();
            case BROKEN:
                return StatusColor.YELLOW.value();
            default:
                return StatusColor.PURPLE.value();
        }
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


}
