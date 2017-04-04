package io.qameta.allure;

import io.qameta.allure.config.ConfigLoader;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author charlie (Dmitry Baev).
 */
public class Commands {

    private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);

    private final Path allureHome;

    public Commands(final Path allureHome) {
        this.allureHome = allureHome;
    }

    public CommandlineConfig getConfig(final String profile) throws IOException {
        if (Objects.isNull(allureHome) || Files.notExists(allureHome)) {
            return new CommandlineConfig();
        }
        return new ConfigLoader(allureHome, profile).load();
    }

    public ExitCode generate(final Path reportDirectory,
                             final List<Path> resultsDirectories,
                             final boolean clean) {
        if (clean && Files.exists(reportDirectory)) {
            FileUtils.deleteQuietly(reportDirectory.toFile());
        }
        ReportGenerator generator = new ReportGenerator(new DefaultConfiguration());
        try {
            generator.generate(reportDirectory, resultsDirectories);
        } catch (IOException e) {
            LOGGER.error("Could not generate report: {}", e);
            return ExitCode.GENERIC_ERROR;
        }
        LOGGER.info("Report successfully generated to {}", reportDirectory);
        return ExitCode.NO_ERROR;
    }

    public ExitCode serve(final List<Path> resultsDirectories, final int port) {
        LOGGER.info("Generating report to temp directory...");

        final Path reportDirectory;
        try {
            reportDirectory = Files.createTempDirectory("allure-commandline");
            reportDirectory.toFile().deleteOnExit();
        } catch (IOException e) {
            LOGGER.error("Could not create temp directory: {}", e);
            return ExitCode.GENERIC_ERROR;
        }

        final ExitCode exitCode = generate(
                reportDirectory,
                resultsDirectories,
                false
        );
        if (exitCode.isSuccess()) {
            return open(reportDirectory, port);
        }
        return exitCode;
    }

    public ExitCode open(final Path reportDirectory, final int port) {
        LOGGER.info("Starting web server...");
        final Server server = setUpServer(port, reportDirectory);
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.error("Could not serve the report: {}", e);
            return ExitCode.GENERIC_ERROR;
        }

        try {
            openBrowser(server.getURI());
        } catch (IOException e) {
            LOGGER.error(
                    "Could not open the report in browser, try to open it manually {}: {}",
                    server.getURI(),
                    e
            );
        }
        LOGGER.info("Server started at <{}>. Press <Ctrl+C> to exit", server.getURI());
        try {
            server.join();
        } catch (InterruptedException e) {
            LOGGER.error("Report serve interrupted {}", e);
            return ExitCode.GENERIC_ERROR;
        }
        return ExitCode.NO_ERROR;
    }

    public ExitCode listPlugins(final String profile) {
        try {
            final CommandlineConfig config = getConfig(profile);
            config.getPlugins().forEach(LOGGER::info);
        } catch (IOException e) {
            LOGGER.error("Can't read config: {}", e);
            return ExitCode.GENERIC_ERROR;
        }
        return ExitCode.NO_ERROR;
    }

    /**
     * Set up Jetty server to serve Allure Report
     */
    public Server setUpServer(final int port, final Path reportDirectory) {
        final Server server = new Server(port);
        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setResourceBase(reportDirectory.toAbsolutePath().toString());
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
        server.setStopAtShutdown(true);
        server.setHandler(handlers);
        return server;
    }

    /**
     * Open the given url in default system browser.
     */
    public void openBrowser(final URI url) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(url);
        } else {
            LOGGER.error("Can not open browser because this capability is not supported on "
                    + "your platform. You can use the link below to open the report manually.");
        }
    }
}
