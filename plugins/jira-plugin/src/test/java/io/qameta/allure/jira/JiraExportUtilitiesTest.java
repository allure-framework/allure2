package io.qameta.allure.jira;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.qameta.allure.jira.TestData.createTestResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JiraExportUtilitiesTest {


    @Test
    public void statusShouldMatchCorrectColor() {
        assertThat(JiraExportUtility.findColorForStatus(Status.FAILED)).isEqualTo(StatusColor.RED.value());
        assertThat(JiraExportUtility.findColorForStatus(Status.PASSED)).isEqualTo(StatusColor.GREEN.value());
        assertThat(JiraExportUtility.findColorForStatus(Status.SKIPPED)).isEqualTo(StatusColor.GRAY.value());
        assertThat(JiraExportUtility.findColorForStatus(Status.BROKEN)).isEqualTo(StatusColor.YELLOW.value());
        assertThat(JiraExportUtility.findColorForStatus(Status.UNKNOWN)).isEqualTo(StatusColor.PURPLE.value());
    }

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

        final Statistic statistic = JiraExportUtility.getStatistic(Arrays.asList(launchResults));
        List<LaunchStatisticExport> launchStatisticExports = JiraExportUtility.convertStatistics(statistic);

        assertThat(launchStatisticExports).isNotEmpty().hasSize(5);

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getStatus)
                .contains(Status.PASSED.value(),
                        Status.FAILED.value(),
                        Status.SKIPPED.value(),
                        Status.BROKEN.value(),
                        Status.UNKNOWN.value());

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getColor)
                .contains(StatusColor.RED.value(),
                        StatusColor.GREEN.value(),
                        StatusColor.GRAY.value(),
                        StatusColor.YELLOW.value(),
                        StatusColor.PURPLE.value());
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

        final Statistic statistic = JiraExportUtility.getStatistic(Arrays.asList(launchResults));
        final List<LaunchStatisticExport> launchStatisticExports = JiraExportUtility.convertStatistics(statistic);

        assertThat(launchStatisticExports).isNotEmpty().hasSize(3);
        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getStatus)
                .contains(Status.PASSED.value(),
                        Status.FAILED.value(),
                        Status.UNKNOWN.value())
                .doesNotContain(Status.SKIPPED.value(), Status.BROKEN.value());

        assertThat(launchStatisticExports).extracting(LaunchStatisticExport::getColor)
                .contains(StatusColor.RED.value(),
                        StatusColor.GREEN.value(),
                        StatusColor.PURPLE.value())
                .doesNotContain(StatusColor.GRAY.value(), StatusColor.YELLOW.value());

        launchStatisticExports.forEach(launchStatisticExport -> assertThat(launchStatisticExport.getCount()).isEqualTo(resultCount));
    }




}
