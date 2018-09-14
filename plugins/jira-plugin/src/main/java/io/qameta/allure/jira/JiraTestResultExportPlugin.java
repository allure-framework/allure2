package io.qameta.allure.jira;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.jira.commons.JiraServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.util.PropertyUtils.getProperty;

/**
 * Plugins exports TestResult information to Jira Ticket.
 */
@SuppressWarnings("PMD.UncommentedEmptyMethodBody")
public class JiraTestResultExportPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraLaunchExportPlugin.class);

    private static final String ALLURE_JIRA_TEST_RESULT_ENABLED = "ALLURE_JIRA_TESTRESULT_ENABLED";

    private static final String EXECUTORS_BLOCK_NAME = "executor";

    private final JiraService jiraService;

    public JiraTestResultExportPlugin() {
        this(JiraServiceUtils.newInstance(JiraService.class));
    }

    public JiraTestResultExportPlugin(final JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {

        if (getProperty(ALLURE_JIRA_TEST_RESULT_ENABLED).map(Boolean::parseBoolean).orElse(false)) {
            exportTestResultsToJira(launchesResults);
        }
    }

    private void exportTestResultsToJira(final List<LaunchResults> launchesResults) {
        final Optional<ExecutorInfo> executorInfo = launchesResults.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .findFirst();

        executorInfo.ifPresent(info -> {
            final List<TestResult> testResults = launchesResults.stream()
                    .map(LaunchResults::getAllResults)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            exportTestResultsToJira(info, testResults);
        });
    }

    private void exportTestResultsToJira(final ExecutorInfo executorInfo,
                                         final List<TestResult> testResults) {
        testResults.stream().filter(this::hasIssueLinks)
                .map(testResult -> convertToJiraTestResult(executorInfo, testResult))
                .flatMap(Collection::stream)
                .forEach(this::exportTestResultToJira);
    }

    private void exportTestResultToJira(final JiraTestResult jiraTestResult) {
        final String issueKey = jiraTestResult.getIssueKey();
        try {
            final JiraTestResult created = jiraService.createTestResult(jiraTestResult);
            LOGGER.info(String.format("Allure test result '%s' synced with issue '%s' successfully",
                    created.getId(), issueKey));
        } catch (Throwable e) {
            LOGGER.error(String.format("Allure test result sync with issue '%s' failed", issueKey), e);
        }
    }

    private List<JiraTestResult> convertToJiraTestResult(final ExecutorInfo executorInfo,
                                                         final TestResult testResult) {
        return testResult.getLinks().stream()
                .filter(this::isIssueLink)
                .map(Link::getName)
                .map(issueKey -> {
                    final JiraTestResult jiraTestResult = new JiraTestResult();
                    jiraTestResult.setIssueKey(issueKey);
                    jiraTestResult.setName(testResult.getName());
                    jiraTestResult.setUrl(getJiraTestResultUrl(executorInfo.getReportUrl(), testResult.getUid()));
                    jiraTestResult.setLaunchName(executorInfo.getBuildName());
                    jiraTestResult.setLaunchUrl(executorInfo.getReportUrl());
                    jiraTestResult.setStatus(testResult.getStatus());
                    jiraTestResult.setDate(testResult.getTime().getStop());
                    return jiraTestResult;
                })
                .collect(Collectors.toList());
    }

    private String getJiraTestResultUrl(final String reportUrl, final String uuid) {
        final String pattern = reportUrl.endsWith("index.html") ? "%s#testresult/%s" : "%s/#testresult/%s";
        return String.format(pattern, reportUrl, uuid);
    }

    private boolean hasIssueLinks(final TestResult testResult) {
        return hasIssueLinks(testResult.getLinks());
    }

    private boolean hasIssueLinks(final List<Link> links) {
        return links.stream().anyMatch(this::isIssueLink);
    }

    private boolean isIssueLink(final Link link) {
        return "issue".equals(link.getType());
    }

}
