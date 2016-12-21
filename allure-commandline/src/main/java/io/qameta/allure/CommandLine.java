package io.qameta.allure;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import io.qameta.allure.command.AllureCommand;
import io.qameta.allure.command.AllureCommandException;
import io.qameta.allure.command.Context;
import io.qameta.allure.command.Help;
import io.qameta.allure.command.ListPlugins;
import io.qameta.allure.command.ReportGenerate;
import io.qameta.allure.command.ReportOpen;
import io.qameta.allure.command.ReportServe;
import io.qameta.allure.command.Version;
import io.qameta.allure.utils.AutoCleanablePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.qameta.allure.command.ExitCode.ARGUMENT_PARSING_ERROR;
import static io.qameta.allure.command.ExitCode.GENERIC_ERROR;
import static io.qameta.allure.utils.AutoCleanablePath.create;

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
        String allureHome = Objects.requireNonNull(
                System.getenv("APP_HOME"),
                "APP_HOME should not be a null"
        );
        Path home = Paths.get(allureHome);
        if (!Files.isDirectory(home)) {
            throw new AllureCommandException("APP_HOME is not a directory");
        }

        try (AutoCleanablePath workDirectory = create("allure-commandline")) {
            Path pluginsDirectory = home.resolve("plugins");
            Path webDirectory = home.resolve("web");
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
