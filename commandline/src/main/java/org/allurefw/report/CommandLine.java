package org.allurefw.report;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import org.allurefw.report.command.AllureCommand;
import org.allurefw.report.command.AllureCommandException;
import org.allurefw.report.command.Context;
import org.allurefw.report.command.Help;
import org.allurefw.report.command.ListPlugins;
import org.allurefw.report.command.ReportGenerate;
import org.allurefw.report.command.ReportOpen;
import org.allurefw.report.command.ReportServe;
import org.allurefw.report.command.Version;
import org.allurefw.report.utils.AutoCleanablePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.properties.PropertyLoader;

import java.nio.file.Path;

import static org.allurefw.report.command.ExitCode.ARGUMENT_PARSING_ERROR;
import static org.allurefw.report.command.ExitCode.GENERIC_ERROR;
import static org.allurefw.report.utils.AutoCleanablePath.create;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
public class CommandLine {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLine.class);

    private static final String COMMANDLINE_NAME = "allure";

    private final Cli<AllureCommand> parser;

    public CommandLine() {
        CliBuilder<AllureCommand> builder = Cli.<AllureCommand>builder(COMMANDLINE_NAME)
                .withDefaultCommand(Help.class)
                .withCommand(Help.class)
                .withCommand(Version.class)
                .withCommand(ListPlugins.class)
                .withCommand(ReportOpen.class)
                .withCommand(ReportGenerate.class)
                .withCommand(ReportServe.class);

        this.parser = builder.build();
    }

    public AllureCommand parse(String... args) {
        return parser.parse(args);
    }

    public static void main(String[] args) throws InterruptedException {
        CommandProperties properties = PropertyLoader.newInstance()
                .populate(CommandProperties.class);

        try (AutoCleanablePath workDirectory = create("allure-commandline")) {
            Path pluginsDirectory = properties.getAllureHome().resolve("plugins");
            Path webDirectory = properties.getAllureHome().resolve("web");
            String toolVersion = CommandLine.class.getPackage().getImplementationVersion();

            Context context = new Context(workDirectory.getPath(), pluginsDirectory,
                    webDirectory, toolVersion, null);

            new CommandLine().parse(args).run(context);
        } catch (AllureCommandException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            System.exit(GENERIC_ERROR.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            System.exit(ARGUMENT_PARSING_ERROR.getCode());
        }
    }
}
