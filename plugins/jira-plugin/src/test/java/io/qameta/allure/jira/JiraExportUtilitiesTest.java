package io.qameta.allure.jira;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.qameta.allure.jira.TestData.createTestResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraExportUtilitiesTest {


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
        when(launchResults.getAllResults()).thenReturn(results);

        final Statistic statistic = JiraExportUtils.getStatistic(Arrays.asList(launchResults));
        List<LaunchStatisticExport> launchStatisticExports = JiraExportUtils.convertStatistics(statistic);

        assertThat(launchStatisticExports).isNotEmpty().hasSize(5);

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getStatus)
                .contains(Status.PASSED.value(),
                        Status.FAILED.value(),
                        Status.SKIPPED.value(),
                        Status.BROKEN.value(),
                        Status.UNKNOWN.value());

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getColor)
                .contains(ResultStatus.FAILED.color(),
                        ResultStatus.PASSED.color(),
                        ResultStatus.SKIPPED.color(),
                        ResultStatus.BROKEN.color(),
                        ResultStatus.UNKNOWN.color());
        launchStatisticExports.forEach(launchStatisticExport -> assertThat(launchStatisticExport.getCount()).isEqualTo(resultCount));

    }

    @Test
    public void emptyTestResultsShouldBeIgnoredWhenConvertingToLaunchStatisticExport() {
        final long resultCount = 1;
        final LaunchResults launchResults = mock(LaunchResults.class);
        final TestResult passed = createTestResult(Status.PASSED);
        final TestResult failed = createTestResult(Status.FAILED);
        final TestResult unknown = createTestResult(Status.UNKNOWN);

        final Set<TestResult> results = new HashSet<>(Arrays.asList(passed, failed, unknown));
        when(launchResults.getAllResults()).thenReturn(results);

        final Statistic statistic = JiraExportUtils.getStatistic(Arrays.asList(launchResults));
        final List<LaunchStatisticExport> launchStatisticExports = JiraExportUtils.convertStatistics(statistic);

        assertThat(launchStatisticExports).isNotEmpty().hasSize(3);
        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getStatus)
                .contains(Status.PASSED.value(),
                        Status.FAILED.value(),
                        Status.UNKNOWN.value())
                .doesNotContain(Status.SKIPPED.value(), Status.BROKEN.value());

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getColor)
                .contains(ResultStatus.FAILED.color(),
                        ResultStatus.PASSED.color(),
                        ResultStatus.UNKNOWN.color())
                .doesNotContain(ResultStatus.SKIPPED.color(), ResultStatus.BROKEN.color());

        launchStatisticExports.forEach(launchStatisticExport -> assertThat(launchStatisticExport.getCount()).isEqualTo(resultCount));
    }


}
