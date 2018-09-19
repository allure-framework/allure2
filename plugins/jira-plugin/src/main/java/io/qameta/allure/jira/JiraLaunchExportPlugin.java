package io.qameta.allure.jira;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.qameta.allure.util.PropertyUtils.getProperty;
import static io.qameta.allure.util.PropertyUtils.requireProperty;

/**
 * Plugins exports Launch information to Jira Ticket.
 */
public class JiraLaunchExportPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraLaunchExportPlugin.class);

    private static final String ALLURE_JIRA_LAUNCH_ENABLED = "ALLURE_JIRA_LAUNCH_ENABLED";
    private static final String ALLURE_JIRA_LAUNCH_ISSUES = "ALLURE_JIRA_LAUNCH_ISSUES";

    private static final String EXECUTORS_BLOCK_NAME = "executor";

    private JiraService jiraService;

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        if (getProperty(ALLURE_JIRA_LAUNCH_ENABLED).map(Boolean::parseBoolean).orElse(false)) {
            setDefaultJiraServiceIfRequired();
            final List<String> issues = splitByComma(requireProperty(ALLURE_JIRA_LAUNCH_ISSUES));
            issues.forEach(issue -> exportLaunchesToJira(issue, launchesResults));
        }
    }


    protected void setJiraService(final JiraService jiraService) {
        this.jiraService = jiraService;
    }

    private void setDefaultJiraServiceIfRequired() {
        if (Objects.isNull(this.jiraService)) {
            setJiraService(new JiraServiceBuilder().defaults().build());
        }
    }

    private void exportLaunchesToJira(final String issueKey,
                                      final List<LaunchResults> launchesResults) {
        final ExecutorInfo info = launchesResults.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .findFirst()
                .orElse(new ExecutorInfo());
        final Statistic statistic = new Statistic();
        launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(statistic::update);
        exportLaunchToJira(issueKey, info, statistic);
    }

    private void exportLaunchToJira(final String issueKey,
                                    final ExecutorInfo executorInfo,
                                    final Statistic statistic) {
        final JiraLaunch jiraLaunch = new JiraLaunch();
        jiraLaunch.setIssueKey(issueKey);
        jiraLaunch.setName(executorInfo.getBuildName());
        jiraLaunch.setUrl(executorInfo.getReportUrl());
        jiraLaunch.setPassed(statistic.getPassed());
        jiraLaunch.setFailed(statistic.getFailed());
        jiraLaunch.setBroken(statistic.getBroken());
        jiraLaunch.setSkipped(statistic.getSkipped());
        jiraLaunch.setUnknown(statistic.getUnknown());
        jiraLaunch.setDate(System.currentTimeMillis());

        exportLaunchToJira(issueKey, jiraLaunch);
    }

    private void exportLaunchToJira(final String issueKey,
                                    final JiraLaunch jiraLaunch) {
        try {
            final JiraLaunch created = jiraService.createJiraLaunch(jiraLaunch);
            LOGGER.info(String.format("Allure launch '%s' synced with issue '%s' successfully",
                    created.getId(), issueKey));
        } catch (Throwable e) {
            LOGGER.error(String.format("Allure launch sync with issue '%s' error", issueKey), e);
        }
    }

    private static List<String> splitByComma(final String value) {
        return Arrays.asList(value.split(","));
    }

}
