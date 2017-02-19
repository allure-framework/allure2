package io.qameta.allure.command;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import io.qameta.allure.Main;
import io.qameta.allure.utils.DeleteVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.qameta.allure.utils.CommandUtils.copyDirectory;
import static io.qameta.allure.utils.CommandUtils.createMain;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
@Command(name = "generate", description = "Generate report")
public class ReportGenerate implements AllureCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerate.class);

    @Inject
    private ResultsOptions resultsOptions = new ResultsOptions();

    @Option(
            title = "Clean report directory",
            name = {"-c", "--clean"},
            description = "Clean Allure report directory before generate new one."
    )
    private boolean cleanReportDirectory;

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
        Path output = Paths.get(reportDirectory);

        if (Files.exists(output) && cleanReportDirectory) {
            Files.walkFileTree(output, new DeleteVisitor());
        }

        Main main = createMain(context);
        main.generate(output, resultsOptions.getResultsDirectories());

        copyDirectory(context.getWebDirectory(), output);
        LOGGER.info("Report successfully generated to the directory <{}>. " +
                "Use `allure open` command to show the report.", reportDirectory);
    }
}
