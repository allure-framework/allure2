package io.qameta.allure;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.qameta.allure.command.GenerateCommand;
import io.qameta.allure.command.MainCommand;
import io.qameta.allure.command.OpenCommand;
import io.qameta.allure.command.ServeCommand;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Artem Eroshenko <eroshenkoam@qameta.io>
 */
public class CommandLine {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLine.class);

    public static final String PROGRAM_NAME = "allure";
    public static final String SERVE_COMMAND = "serve";
    public static final String GENERATE_COMMAND = "generate";
    public static final String OPEN_COMMAND = "open";

    private final MainCommand mainCommand;
    private final ServeCommand serveCommand;
    private final GenerateCommand generateCommand;
    private final OpenCommand openCommand;

    private final Commands commands;

    private final JCommander commander;

    public CommandLine() {
        this(new Commands());
    }

    public CommandLine(final Commands commands) {
        this.commands = commands;
        this.mainCommand = new MainCommand();
        this.serveCommand = new ServeCommand();
        this.generateCommand = new GenerateCommand();
        this.openCommand = new OpenCommand();
        this.commander = new JCommander(mainCommand);
        this.commander.addCommand(GENERATE_COMMAND, generateCommand);
        this.commander.addCommand(SERVE_COMMAND, serveCommand);
        this.commander.addCommand(OPEN_COMMAND, openCommand);
        this.commander.setProgramName(PROGRAM_NAME);
    }

    public static void main(final String[] args) throws InterruptedException {
        final CommandLine commandLine = new CommandLine();
        final ExitCode exitCode = commandLine
                .parse(args)
                .orElseGet(commandLine::run);
        System.exit(exitCode.getCode());
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Optional<ExitCode> parse(final String... args) {
        try {
            commander.parse(args);
        } catch (ParameterException e) {
            LOGGER.debug("Error during arguments parsing: {}", e);
            LOGGER.info("Could not parse arguments: {}", e.getMessage());
            printUsage(commander);
            return Optional.of(ExitCode.ARGUMENT_PARSING_ERROR);
        }

        //Hack to limit count of main parameters
        final List<Path> reportDirectories = openCommand.getReportDirectories();
        if (reportDirectories.size() != 1) {
            LOGGER.error("Only one main argument is allowed");
            return Optional.of(ExitCode.ARGUMENT_PARSING_ERROR);
        }

        return Optional.empty();
    }

    public ExitCode run() {
        if (mainCommand.getVerboseOptions().isQuiet()) {
            LogManager.getRootLogger().setLevel(Level.OFF);
        }

        if (mainCommand.getVerboseOptions().isVerbose()) {
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        }

        if (mainCommand.isVersion()) {
            String toolVersion = CommandLine.class.getPackage().getImplementationVersion();
            LOGGER.info(Objects.isNull(toolVersion) ? "unknown" : toolVersion);
            return ExitCode.NO_ERROR;
        }

        if (mainCommand.isHelp()) {
            printUsage(commander);
            return ExitCode.NO_ERROR;
        }

        final String parsedCommand = commander.getParsedCommand();
        switch (parsedCommand) {
            case GENERATE_COMMAND:
                return commands.generate(
                        generateCommand.getReportDirectory(),
                        generateCommand.getResultsOptions().getResultsDirectories(),
                        generateCommand.isCleanReportDirectory()
                );
            case SERVE_COMMAND:
                return commands.serve(
                        serveCommand.getResultsOptions().getResultsDirectories(),
                        serveCommand.getPortOptions().getPort()
                );
            case OPEN_COMMAND:
                return commands.open(
                        openCommand.getReportDirectories().get(0),
                        openCommand.getPortOptions().getPort()
                );
            default:
                printUsage(commander);
                return ExitCode.ARGUMENT_PARSING_ERROR;
        }
    }

    public JCommander getCommander() {
        return commander;
    }

    public MainCommand getMainCommand() {
        return mainCommand;
    }

    private void printUsage(final JCommander commander) {
        final String parsedCommand = commander.getParsedCommand();
        if (Objects.isNull(parsedCommand)) {
            commander.usage();
        } else {
            commander.usage(parsedCommand);
        }
    }
}
