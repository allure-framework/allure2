package io.qameta.allure.jira;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.qameta.allure.util.PropertyUtils.getProperty;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */
public class JiraExportPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraExportPlugin.class);

    private static final String EXECUTORS_BLOCK_NAME = "executor";

    private static final String ALLURE_JIRA_ENABLED = "ALLURE_JIRA_ENABLED";
    private static final String ALLURE_JIRA_LAUNCH_ISSUES = "ALLURE_JIRA_LAUNCH_ISSUES";

    private final Supplier<JiraService> jiraServiceSupplier;

    public JiraExportPlugin() {
        this(() -> new JiraServiceBuilder().defaults().build());
    }

    public JiraExportPlugin(final Supplier<JiraService> jiraServiceSupplier) {
        this.jiraServiceSupplier = jiraServiceSupplier;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        if (getProperty(ALLURE_JIRA_ENABLED).map(Boolean::parseBoolean).orElse(false)) {
            final JiraService jiraService = jiraServiceSupplier.get();

            final List<String> issues = splitByComma(getProperty(ALLURE_JIRA_LAUNCH_ISSUES).orElse(""));
            final ExecutorInfo executor = getExecutor(launchesResults);
            final Statistic statistic = getStatistic(launchesResults);

            final JiraLaunch launch = getJiraLaunch(issues, executor, statistic);
            final JiraLaunch created = exportLaunchToJira(jiraService, launch);

            getTestResults(launchesResults).stream()
                    .map(testResult -> getJiraTestResult(created, executor, testResult))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(testResult -> exportTestResultToJira(jiraService, testResult));
        }
    }

    private JiraLaunch getJiraLaunch(final List<String> issueKeys,
                                     final ExecutorInfo executor,
                                     final Statistic statistic) {
        return new JiraLaunch()
                .setIssueKeys(issueKeys)
                .setName(executor.getBuildName())
                .setUrl(executor.getReportUrl())
                .setPassed(statistic.getPassed())
                .setFailed(statistic.getFailed())
                .setBroken(statistic.getBroken())
                .setSkipped(statistic.getSkipped())
                .setUnknown(statistic.getUnknown())
                .setDate(System.currentTimeMillis());
    }

    private Optional<JiraTestResult> getJiraTestResult(final JiraLaunch launch,
                                                       final ExecutorInfo executor,
                                                       final TestResult testResult) {
        final List<String> issues = testResult.getLinks().stream()
                .filter(this::isIssueLink)
                .map(Link::getName)
                .collect(Collectors.toList());

        if (issues.isEmpty()) {
            return Optional.empty();
        } else {
            final JiraTestResult jiraTestResult = new JiraTestResult()
                    .setIssueKeys(issues)
                    .setName(testResult.getName())
                    .setUrl(getJiraTestResultUrl(executor.getReportUrl(), testResult.getUid()))
                    .setStatus(testResult.getStatus().toString())
                    .setDate(testResult.getTime().getStop())
                    .setLaunchId(launch.getId());
            return Optional.of(jiraTestResult);
        }
    }


    private List<TestResult> getTestResults(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private ExecutorInfo getExecutor(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .findFirst()
                .orElse(new ExecutorInfo());
    }

    private Statistic getStatistic(final List<LaunchResults> launchesResults) {
        final Statistic statistic = new Statistic();
        launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(statistic::update);
        return statistic;
    }

    private JiraLaunch exportLaunchToJira(final JiraService jiraService, final JiraLaunch launch) {
        try {
            final JiraLaunch created = jiraService.createJiraLaunch(launch);
            LOGGER.info(String.format("Allure launch '%s' synced with issues '%s' successfully",
                    created.getId(), created.getIssueKeys()));
            return created;
        } catch (Throwable e) {
            LOGGER.error(String.format("Allure launch sync with issue '%s' error", launch.getIssueKeys()), e);
            throw e;
        }
    }

    private void exportTestResultToJira(final JiraService jiraService, final JiraTestResult testResult) {
        try {
            final JiraTestResult created = jiraService.createTestResult(testResult);
            LOGGER.info(String.format("Allure test result '%s' synced with issue '%s' successfully",
                    created.getId(),
                    created.getIssueKeys()));
        } catch (Throwable e) {
            LOGGER.error(String.format("Allure test result sync with issue '%s' failed", testResult.getIssueKeys()), e);
            throw e;
        }
    }


    private String getJiraTestResultUrl(final String reportUrl, final String uuid) {
        return Optional.ofNullable(reportUrl)
                .map(url -> url.endsWith("index.html") ? "%s#testresult/%s" : "%s/#testresult/%s")
                .map(pattern -> String.format(pattern, reportUrl, uuid))
                .orElse(null);
    }

    private boolean isIssueLink(final Link link) {
        return "issue".equals(link.getType());
    }

    private static List<String> splitByComma(final String value) {
        return Arrays.asList(value.split(","));
    }

}
