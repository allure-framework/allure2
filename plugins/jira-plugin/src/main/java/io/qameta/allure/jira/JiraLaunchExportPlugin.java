package io.qameta.allure.jira;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.jira.commons.JiraServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static io.qameta.allure.util.PropertyUtils.getProperty;

/**
 * Plugins exports Launch information to Jira Ticket.
 */
public class JiraLaunchExportPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraLaunchExportPlugin.class);

    private static final String ALLURE_JIRA_LAUNCH_ENABLED = "allure.jira.launch.enabled";
    private static final String ALLURE_JIRA_LAUNCH_ISSUE = "allure.jira.launch.issue";

    private static final String EXECUTORS_BLOCK_NAME = "executor";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        if (getProperty(ALLURE_JIRA_LAUNCH_ENABLED).map(Boolean::parseBoolean).orElse(false)) {
            final Optional<String> issueKey = getIssueKey();
            issueKey.ifPresent(key -> exportLaunchesToJira(key, launchesResults));
        }
    }

    private void exportLaunchesToJira(final String issueKey,
                                      final List<LaunchResults> launchesResults) {
        final Optional<ExecutorInfo> executorInfo = launchesResults.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .findFirst();
        executorInfo.ifPresent(info -> {
            final Statistic statistic = new Statistic();
            launchesResults.stream()
                    .map(LaunchResults::getAllResults)
                    .flatMap(Collection::stream)
                    .forEach(statistic::update);
            exportLaunchToJira(issueKey, info, statistic);
        });
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
        final JiraService jiraService = JiraServiceUtils.newInstance(JiraService.class);
        try {
            final JiraLaunch created = jiraService.createJiraLaunch(jiraLaunch);
            LOGGER.info(String.format("Allure launch '%s' synced with issue '%s' successfully",
                    created.getId(), issueKey));
        } catch (Throwable e) {
            LOGGER.error(String.format("Allure launch sync with issue '%s' error", issueKey), e);
        }
    }

    private Optional<String> getIssueKey() {
        final Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.putAll(System.getenv());
        return Optional.ofNullable(properties.getProperty(ALLURE_JIRA_LAUNCH_ISSUE))
                .filter(StringUtils::isNotBlank);
    }

}
