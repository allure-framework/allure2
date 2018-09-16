package io.qameta.allure.xray;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Link;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.jira.JiraIssueComment;
import io.qameta.allure.jira.JiraService;
import io.qameta.allure.jira.JiraServiceBuilder;
import io.qameta.allure.jira.XrayTestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.util.PropertyUtils.getProperty;
import static io.qameta.allure.util.PropertyUtils.requireProperty;

/**
 * Plugin update Xray test run status from test result.
 */
public class XrayTestRunExportPlugin implements Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(XrayTestRunExportPlugin.class);

    private static final String EXECUTORS_BLOCK_NAME = "executor";

    private static final String ALLURE_XRAY_ENABLED = "ALLURE_XRAY_ENABLED";
    private static final String ALLURE_XRAY_EXECUTION_ISSUES = "ALLURE_XRAY_EXECUTION_ISSUES";

    private static final String ALLURE_XRAY_STATUS_UNKNOWN = "ALLURE_XRAY_STATUS_UNKNOWN";
    private static final String ALLURE_XRAY_STATUS_SKIPPED = "ALLURE_XRAY_STATUS_SKIPPED";
    private static final String ALLURE_XRAY_STATUS_BROKEN = "ALLURE_XRAY_STATUS_BROKEN";
    private static final String ALLURE_XRAY_STATUS_FAILED = "ALLURE_XRAY_STATUS_FAILED";
    private static final String ALLURE_XRAY_STATUS_PASSED = "ALLURE_XRAY_STATUS_PASSED";

    private static final String XRAY_STATUS_PASS = "PASS";
    private static final String XRAY_STATUS_FAIL = "FAIL";
    private static final String XRAY_STATUS_TODO = "TODO";

    private final JiraService jiraService;

    public XrayTestRunExportPlugin() {
        this(new JiraServiceBuilder().defaults().build());
    }

    public XrayTestRunExportPlugin(final JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) {
        if (getProperty(ALLURE_XRAY_ENABLED).map(Boolean::parseBoolean).orElse(false)) {
            updateTestRunStatuses(launchesResults);
        }
    }

    private void updateTestRunStatuses(final List<LaunchResults> launchesResults) {
        final List<String> executionIssues = splitByComma(requireProperty(ALLURE_XRAY_EXECUTION_ISSUES));
        final Map<Status, String> statusesMap = getStatusesMap();

        final Map<String, XrayTestRun> testRunsMap = executionIssues.stream()
                .map(jiraService::getTestRunsForTestExecution)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(XrayTestRun::getKey, r -> r));

        launchesResults.stream()
                .map(LaunchResults::getAllResults)
                .flatMap(Collection::stream)
                .forEach(testResult -> updateTestRunStatuses(testResult, statusesMap, testRunsMap));

        final Optional<ExecutorInfo> executorInfo = launchesResults.stream()
                .map(launchResults -> launchResults.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .findFirst();
        executorInfo.ifPresent(info -> executionIssues.forEach(issue -> addExecutionComment(issue, info)));
    }

    private void updateTestRunStatuses(final TestResult testResult,
                                       final Map<Status, String> statusesMap,
                                       final Map<String, XrayTestRun> testRunsMap) {
        final String status = statusesMap.get(testResult.getStatus());
        final List<XrayTestRun> testRunIssueKeys = testResult.getLinks().stream()
                .filter(this::isTmsLink)
                .map(Link::getName)
                .map(testRunsMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        testRunIssueKeys.forEach(testRun -> updateTestRunStatus(testRun, status));
    }

    private void addExecutionComment(final String executionIssueKey, final ExecutorInfo info) {
        try {
            final String message = String.format("Execution updated from launch [%s|%s]",
                    info.getName(), info.getReportUrl());
            jiraService.createIssueComment(executionIssueKey, new JiraIssueComment().setBody(message));
            LOGGER.debug(String.format("Xray execution '%s' commented successfully", executionIssueKey));
        } catch (Exception e) {
            LOGGER.debug(String.format("Xray execution '%s' comment failed", executionIssueKey));
        }
    }

    private void updateTestRunStatus(final XrayTestRun testRun, final String status) {
        if (!status.equals(testRun.getStatus())) {
            try {
                jiraService.updateTestRunStatus(testRun.getId(), status);
                LOGGER.debug(String.format("Xray testrun '%s' status updated to '%s' successfully",
                        testRun.getKey(), status));
            } catch (Exception e) {
                LOGGER.error(String.format("Xray testrun '%s' status update failed",
                        testRun.getKey()));
            }
        }
    }

    private static Map<Status, String> getStatusesMap() {
        final Map<Status, String> statues = new HashMap<>();
        statues.put(Status.PASSED, getProperty(ALLURE_XRAY_STATUS_PASSED).orElse(XRAY_STATUS_PASS));
        statues.put(Status.FAILED, getProperty(ALLURE_XRAY_STATUS_FAILED).orElse(XRAY_STATUS_FAIL));
        statues.put(Status.BROKEN, getProperty(ALLURE_XRAY_STATUS_BROKEN).orElse(XRAY_STATUS_FAIL));
        statues.put(Status.SKIPPED, getProperty(ALLURE_XRAY_STATUS_SKIPPED).orElse(XRAY_STATUS_TODO));
        statues.put(Status.UNKNOWN, getProperty(ALLURE_XRAY_STATUS_UNKNOWN).orElse(XRAY_STATUS_TODO));
        return statues;
    }

    private boolean isTmsLink(final Link link) {
        return "tms".equals(link.getType());
    }

    private static List<String> splitByComma(final String value) {
        return Arrays.asList(value.split(","));
    }
}
