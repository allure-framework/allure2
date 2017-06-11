package io.qameta.allure.historytrend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.HistoryTrendItem;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.history.HistoryTrendPlugin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class HistoryTrendPluginTest {

    private static final TypeReference<List<HistoryTrendItem>> HISTORY_TYPE =
            new TypeReference<List<HistoryTrendItem>>() {
            };

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldCreateHistoryTrendData() throws IOException {
        Configuration configuration = new ConfigurationBuilder().useDefault().build();
        Statistic expectedStat = new Statistic()
                .withPassed(1)
                .withFailed(1);

        ExecutorInfo executorInfo = new ExecutorInfo()
                .withBuildId("1")
                .withBuildUrl("http://jenkins.test.url/job/test/1/");
        Map<String, Object> extras = ImmutableMap.of("executor", executorInfo);

        List<LaunchResults> launchResults = createSingleLaunchResults(extras,
                createTestResult("first", Status.PASSED, 1L, 9L),
                createTestResult("second", Status.FAILED, 11L, 19L));

        File historyTrend = createHistoryTrendFile(configuration, launchResults);
        JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        assertThat(historyTrend).as("history-trend.json has not been created").exists();
        try (InputStream is = Files.newInputStream(historyTrend.toPath())) {
            final List<HistoryTrendItem> history = jacksonContext.getValue().readValue(is, HISTORY_TYPE);
            assertThat(history).as("Single history trend record should be created").hasSize(1);
            assertStatistics(history, expectedStat);
            assertExecutorInfo(history, executorInfo);
        }
    }

    @Test
    public void shouldExtractLatestExecutorInfo() throws IOException {
        Configuration configuration = new ConfigurationBuilder().useDefault().build();
        Statistic expectedStat = new Statistic()
                .withPassed(2)
                .withFailed(1)
                .withBroken(1);

        ExecutorInfo executor1 = new ExecutorInfo()
                .withBuildId("1")
                .withBuildUrl("http://jenkins.test.url/job/test/1/");
        ExecutorInfo executor2 = new ExecutorInfo()
                .withBuildId("2")
                .withBuildUrl("http://jenkins.test.url/job/test/2/");
        Map<String, Object> extra1 = ImmutableMap.of("executor", executor1);
        Map<String, Object> extra2 = ImmutableMap.of("executor", executor2);

        List<LaunchResults> launches = createSingleLaunchResults(extra1,
                createTestResult("first", Status.PASSED, 1L, 5L),
                createTestResult("second", Status.FAILED, 7L, 11L)
        );
        launches.addAll(createSingleLaunchResults(extra2,
                createTestResult("third", Status.PASSED, 15L, 20L),
                createTestResult("fourth", Status.BROKEN, 23L, 27L)
        ));

        File historyTrend = createHistoryTrendFile(configuration, launches);
        JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        assertThat(historyTrend).as("history-trend.json has not been created").exists();
        try (InputStream is = Files.newInputStream(historyTrend.toPath())) {
            final List<HistoryTrendItem> history = jacksonContext.getValue().readValue(is, HISTORY_TYPE);
            assertThat(history).as("Single history trend record should be created").hasSize(1);
            assertStatistics(history, expectedStat);
            assertExecutorInfo(history, executor2);
        }
    }

    @Test
    public void shouldCollectPreviousHistoryItems() throws IOException {
        Configuration configuration = new ConfigurationBuilder().useDefault().build();

        List<HistoryTrendItem> previousItems = new ArrayList<>();
        previousItems.add(new HistoryTrendItem().withStatistics(new Statistic().withPassed(1)));

        Map<String, Object> extra = ImmutableMap.of("history-trend", previousItems);

        List<LaunchResults> launches = createSingleLaunchResults(extra,
                createTestResult("first", Status.PASSED, 1L, 5L)
        );

        File historyTrend = createHistoryTrendFile(configuration, launches);
        JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        assertThat(historyTrend).as("history-trend.json has not been created").exists();
        try (InputStream is = Files.newInputStream(historyTrend.toPath())) {
            final List<HistoryTrendItem> history = jacksonContext.getValue().readValue(is, HISTORY_TYPE);
            assertThat(history).as("Previous history trend items should be collected").hasSize(2);
        }
    }

    private void assertStatistics(List<HistoryTrendItem> history, Statistic expectedStat) {
        assertThat(history)
                .extracting(HistoryTrendItem::getStatistics).first()
                .as("Unexpected statistics calculated from test results")
                .isEqualToComparingFieldByField(expectedStat);
    }

    private void assertExecutorInfo(List<HistoryTrendItem> history, ExecutorInfo executorInfo) {
        assertThat(history)
                .extracting(HistoryTrendItem::getExecutorInfo).first()
                .as("Unexpected executor info in history trend i")
                .isEqualToComparingFieldByField(executorInfo);
    }

    private File createHistoryTrendFile(Configuration configuration, List<LaunchResults> launchResults)
            throws IOException {
        HistoryTrendPlugin plugin = new HistoryTrendPlugin();
        File resultsFolder = folder.newFolder();
        plugin.aggregate(configuration, launchResults, resultsFolder.toPath());
        return new File(resultsFolder, "history/history-trend.json");
    }

    private TestResult createTestResult(String name, Status status, long start, long stop) {
        return new TestResult().withName(name).withHistoryId(UUID.randomUUID().toString()).
                withTime(new Time().withStart(start).withStop(stop)).withStatus(status);
    }
}
