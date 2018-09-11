package io.qameta.allure.jira;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Plugins exports TestResult information to Jira Ticket.
 */
@SuppressWarnings("PMD.UncommentedEmptyMethodBody")
public class JiraTestResultExportPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {

    }

}
