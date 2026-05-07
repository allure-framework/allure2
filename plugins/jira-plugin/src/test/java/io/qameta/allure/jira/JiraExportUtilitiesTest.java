/*
 *  Copyright 2016-2026 Qameta Software Inc
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

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qameta.allure.jira.TestData.createTestResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraExportUtilitiesTest {

    /**
     * Verifies all non-empty status counts are converted to Jira launch statistics.
     * The test checks status, color, and count values for each Allure status.
     */
    @Description
    @Test
    public void testResultsFromLaunchResultsShouldConvertToLaunchStatisticExport() {
        final long resultCount = 1;
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult passed = createTestResult(Status.PASSED);
        final TestResult failed = createTestResult(Status.FAILED);
        final TestResult broken = createTestResult(Status.BROKEN);
        final TestResult skipped = createTestResult(Status.SKIPPED);
        final TestResult unknown = createTestResult(Status.UNKNOWN);

        final Set<TestResult> results = new HashSet<>(Arrays.asList(passed, failed, broken, skipped, unknown));
        createLaunchResults(launchResults, results);

        final List<LaunchStatisticExport> launchStatisticExports = convertStatistics(launchResults);

        assertThat(launchStatisticExports).isNotEmpty().hasSize(5);

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getStatus)
                .contains(
                        Status.PASSED.value(),
                        Status.FAILED.value(),
                        Status.SKIPPED.value(),
                        Status.BROKEN.value(),
                        Status.UNKNOWN.value()
                );

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getColor)
                .contains(
                        ResultStatus.FAILED.color(),
                        ResultStatus.PASSED.color(),
                        ResultStatus.SKIPPED.color(),
                        ResultStatus.BROKEN.color(),
                        ResultStatus.UNKNOWN.color()
                );
        launchStatisticExports.forEach(launchStatisticExport -> assertThat(launchStatisticExport.getCount()).isEqualTo(resultCount));

    }

    /**
     * Verifies statuses with zero counts are omitted from Jira launch statistics.
     * The test checks absent skipped and broken statuses are not exported.
     */
    @Description
    @Test
    public void emptyTestResultsShouldBeIgnoredWhenConvertingToLaunchStatisticExport() {
        final long resultCount = 1;
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult passed = createTestResult(Status.PASSED);
        final TestResult failed = createTestResult(Status.FAILED);
        final TestResult unknown = createTestResult(Status.UNKNOWN);

        final Set<TestResult> results = new HashSet<>(Arrays.asList(passed, failed, unknown));
        createLaunchResults(launchResults, results);

        final List<LaunchStatisticExport> launchStatisticExports = convertStatistics(launchResults);

        assertThat(launchStatisticExports).isNotEmpty().hasSize(3);
        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getStatus)
                .contains(
                        Status.PASSED.value(),
                        Status.FAILED.value(),
                        Status.UNKNOWN.value()
                )
                .doesNotContain(Status.SKIPPED.value(), Status.BROKEN.value());

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getColor)
                .contains(
                        ResultStatus.FAILED.color(),
                        ResultStatus.PASSED.color(),
                        ResultStatus.UNKNOWN.color()
                )
                .doesNotContain(ResultStatus.SKIPPED.color(), ResultStatus.BROKEN.color());

        launchStatisticExports.forEach(launchStatisticExport -> assertThat(launchStatisticExport.getCount()).isEqualTo(resultCount));
    }

    private void createLaunchResults(final LaunchResults launchResults, final Set<TestResult> results) {
        Allure.step("Create launch results for Jira statistics", () -> {
            when(launchResults.getAllResults()).thenReturn(results);
            Allure.addAttachment("jira-statistic-input.txt", "text/plain", describeResults(results));
        });
    }

    private List<LaunchStatisticExport> convertStatistics(final LaunchResults launchResults) {
        return Allure.step("Convert Jira launch statistics", () -> {
            final Statistic statistic = JiraExportUtils.getStatistic(Arrays.asList(launchResults));
            final List<LaunchStatisticExport> exports = JiraExportUtils.convertStatistics(statistic);
            Allure.addAttachment("jira-statistic-output.txt", "text/plain", describeStatistic(exports));
            return exports;
        });
    }

    private String describeResults(final Set<TestResult> results) {
        return results.stream()
                .map(result -> String.format("uid=%s, name=%s, status=%s", result.getUid(), result.getName(), result.getStatus()))
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeStatistic(final List<LaunchStatisticExport> statistic) {
        return statistic.stream()
                .map(
                        item -> String.format(
                                "status=%s, color=%s, count=%s",
                                item.getStatus(),
                                item.getColor(),
                                item.getCount()
                        )
                )
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
