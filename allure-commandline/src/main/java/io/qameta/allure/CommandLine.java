/*
 *  Copyright 2016-2026 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.qameta.allure.command.GenerateCommand;
import io.qameta.allure.command.MainCommand;
import io.qameta.allure.command.OpenCommand;
import io.qameta.allure.command.PluginCommand;
import io.qameta.allure.command.ServeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * @author eroshenkoam Artem Eroshenko
 */
public class CommandLine {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLine.class);

    protected static final String PROGRAM_NAME = "allure";
    protected static final String SERVE_COMMAND = "serve";
    protected static final String GENERATE_COMMAND = "generate";
    protected static final String OPEN_COMMAND = "open";
    protected static final String PLUGIN_COMMAND = "plugin";

    private final MainCommand mainCommand;
    private final ServeCommand serveCommand;
    private final GenerateCommand generateCommand;
    private final OpenCommand openCommand;
    private final PluginCommand pluginCommand;
    private final Commands commands;
    private final JCommander commander;

    public CommandLine(final Path allureHome) {
        this(new Commands(allureHome));
    }

    public CommandLine(final Commands commands) {
        this.commands = commands;
        this.mainCommand = new MainCommand();
        this.serveCommand = new ServeCommand();
        this.generateCommand = new GenerateCommand();
        this.openCommand = new OpenCommand();
        this.pluginCommand = new PluginCommand();
        this.commander = new JCommander(mainCommand);
        this.commander.addCommand(GENERATE_COMMAND, generateCommand);
        this.commander.addCommand(SERVE_COMMAND, serveCommand);
        this.commander.addCommand(OPEN_COMMAND, openCommand);
        this.commander.addCommand(PLUGIN_COMMAND, pluginCommand);
        this.commander.setProgramName(PROGRAM_NAME);
    }

    public static void main(final String[] args) throws InterruptedException {
        final String allureHome = System.getenv("APP_HOME");
        final CommandLine commandLine;
        if (Objects.isNull(allureHome)) {
            commandLine = new CommandLine((Path) null);
            LOGGER.info("APP_HOME is not set, using default configuration");
        } else {
            commandLine = new CommandLine(Paths.get(allureHome));
        }
        final ExitCode exitCode = commandLine
                .parse(args)
                .orElseGet(commandLine::run);
        System.exit(exitCode.getCode());
    }

    public Optional<ExitCode> parse(final String... args) {
        if (args.length == 0) {
            printUsage(commander);
            return Optional.of(ExitCode.ARGUMENT_PARSING_ERROR);
        }
        try {
            commander.parse(args);
        } catch (ParameterException e) {
            LOGGER.debug("Error during arguments parsing", e);
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

    @SuppressWarnings("PMD.SystemPrintln")
    public ExitCode run() {
        final java.util.logging.Logger rootLogger = initRootLogger();
        if (mainCommand.getVerboseOptions().isQuiet()) {
            rootLogger.setLevel(Level.OFF);
        }

        if (mainCommand.getVerboseOptions().isVerbose()) {
            rootLogger.setLevel(Level.FINE);
        }

        if (mainCommand.isVersion()) {
            final String toolVersion = CommandLine.class.getPackage().getImplementationVersion();
            System.out.println(Objects.isNull(toolVersion) ? "unknown" : toolVersion);
            return ExitCode.NO_ERROR;
        }

        if (mainCommand.isHelp()) {
            printUsage(commander);
            return ExitCode.NO_ERROR;
        }

        final String parsedCommand = commander.getParsedCommand();
        if (Objects.isNull(parsedCommand)) {
            printUsage(commander);
            return ExitCode.ARGUMENT_PARSING_ERROR;
        }
        switch (parsedCommand) {
            case GENERATE_COMMAND:
                return commands.generate(
                        generateCommand.getReportDirectory(),
                        generateCommand.getResultsOptions().getResultsDirectories(),
                        generateCommand.isCleanReportDirectory(),
                        generateCommand.isSingleFileMode(),
                        generateCommand.getConfigOptions(),
                        generateCommand.getReportNameOptions(),
                        generateCommand.getReportLanguageOptions()
                );
            case SERVE_COMMAND:
                return commands.serve(
                        serveCommand.getResultsOptions().getResultsDirectories(),
                        serveCommand.getHostPortOptions().getHost(),
                        serveCommand.getHostPortOptions().getPort(),
                        serveCommand.getConfigOptions(),
                        serveCommand.getReportNameOptions(),
                        serveCommand.getReportLanguageOptions()
                );
            case OPEN_COMMAND:
                return commands.open(
                        openCommand.getReportDirectories().get(0),
                        openCommand.getHostPortOptions().getHost(),
                        openCommand.getHostPortOptions().getPort()
                );
            case PLUGIN_COMMAND:
                return commands.listPlugins(pluginCommand.getConfigOptions());
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
        commander.usage();
    }

    private static java.util.logging.Logger initRootLogger() {
        final java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        final Handler handler = new StdOutHandler();
        handler.setLevel(Level.ALL);
        rootLogger.addHandler(handler);
        return rootLogger;
    }

    /**
     * Print only a message from LogRecord.
     */
    private static final class MessageOnlyFormatter extends Formatter {

        @Override
        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            builder.append(formatMessage(record))
                    .append(System.lineSeparator());

            final Throwable thrown = record.getThrown();
            if (thrown != null) {
                final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(stringWriter);
                thrown.printStackTrace(printWriter);
                printWriter.flush();
                builder.append(stringWriter);
            }

            return builder.toString();
        }
    }

    /**
     * This Handler publishes log records to System.out.
     * By default the MessageOnlyFormatter is used to only print log messages.
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    private static final class StdOutHandler extends StreamHandler {

        StdOutHandler() {
            super(System.out, new MessageOnlyFormatter());
        }

        @Override
        public synchronized void publish(final LogRecord record) {
            super.publish(record);
            flush();
        }

        @Override
        public synchronized void close() {
            flush();
        }
    }
}
