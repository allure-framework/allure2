package org.allurefw.report.command;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Paths;

import static org.allurefw.report.utils.CommandUtils.copyWeb;
import static org.allurefw.report.utils.CommandUtils.createMain;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@Command(name = "generate", description = "Generate report")
public class ReportGenerate implements AllureCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerate.class);

    @Inject
    private ResultsOptions resultsOptions = new ResultsOptions();

    @Option(
            title = "Report directory",
            name = {"-o", "--report-dir", "--output"},
            description = "The directory to generate Allure report into.")
    protected String reportDirectory = "allure-report";

    @Inject
    private VerboseOptions verboseOptions = new VerboseOptions();

    @Override
    public void run(Context context) throws Exception {
        verboseOptions.configureLogLevel();
        createMain(context.getPluginsDirectory(), context.getWorkDirectory())
                .generate(Paths.get(reportDirectory), resultsOptions.getResultsDirectories());
        copyWeb(context.getWebDirectory(), context.getWebDirectory());
        LOGGER.info("Report successfully generated to the directory <{}>. " +
                "Use `allure report open` command to show the report.", reportDirectory);
    }
}
