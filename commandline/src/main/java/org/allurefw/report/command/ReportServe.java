package org.allurefw.report.command;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.allurefw.report.utils.CommandUtils.openBrowser;
import static org.allurefw.report.utils.CommandUtils.setUpServer;

/**
 * @author charlie (Dmitry Baev).
 */
@Command(name = "serve", description = "Serve the report")
public class ReportServe extends ReportGenerate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServe.class);

    @Option(name = {"-p", "--port"}, type = OptionType.COMMAND,
            description = "This port will be used to start web server for the report")
    protected int port = 0;

    @Override
    protected void runUnsafe() throws Exception {
        validateResultsDirectories();
        Path outputDirectory = createTempDirectory("allure-report");
        CommandLine commandLine = createCommandLine(outputDirectory);
        new DefaultExecutor().execute(commandLine);
        LOGGER.info("Report successfully generated.");

        LOGGER.info("Starting web server...");
        Server server = setUpServer(port, outputDirectory);
        server.start();

        openBrowser(server.getURI());
        LOGGER.info("Server started at <{}>. Press <Ctrl+C> to exit ...", server.getURI());
        server.join();
    }
}
