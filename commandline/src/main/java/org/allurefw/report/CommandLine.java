package org.allurefw.report;

import io.airlift.airline.Cli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.allurefw.report.command.AbstractCommand;
import org.allurefw.report.command.AllureCommand;
import org.allurefw.report.command.AllureCommandException;
import org.allurefw.report.command.AllureHelp;
import org.allurefw.report.command.AllureVersion;
import org.allurefw.report.command.ExitCode;
import org.allurefw.report.command.ReportClean;
import org.allurefw.report.command.ReportGenerate;
import org.allurefw.report.command.ReportOpen;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
public class CommandLine {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommand.class);

    private CommandLine() {
    }

    public static void main(String[] args) throws InterruptedException {
        ExitCode exitCode;
        try {
            Cli.CliBuilder<AllureCommand> builder = Cli.<AllureCommand>builder("allure")
                    .withDefaultCommand(AllureHelp.class)
                    .withCommand(AllureHelp.class)
                    .withCommand(AllureVersion.class)
                    .withCommand(ReportGenerate.class);

            builder.withGroup("report")
                    .withDescription("Report commands")
                    .withDefaultCommand(ReportOpen.class)
                    .withCommand(ReportOpen.class)
                    .withCommand(ReportClean.class)
                    .withCommand(ReportGenerate.class);


            Cli<AllureCommand> allureParser = builder.build();
            AllureCommand command = allureParser.parse(args);

            command.run();      //NOSONAR
            exitCode = command.getExitCode();
        } catch (AllureCommandException e) {
            LOGGER.error("{}", e);
            exitCode = ExitCode.GENERIC_ERROR;
        } catch (Exception e) {
            LOGGER.error("{}", e);
            exitCode = ExitCode.ARGUMENT_PARSING_ERROR;
        }

        System.exit(exitCode.getCode());
    }
}
