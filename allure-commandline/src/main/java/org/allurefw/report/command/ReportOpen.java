package org.allurefw.report.command;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
@Command(name = "open", description = "Open generated report")
public class ReportOpen extends ReportCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportOpen.class);

    @Option(name = {"-p", "--port"}, type = OptionType.COMMAND,
            description = "This port will be used to start web server for the report")
    protected int port = 0;

    @Override
    protected void runUnsafe() throws Exception {
        Path reportDirectory = getReportDirectoryPath();
        if (Files.notExists(reportDirectory)) {
            throw new AllureCommandException(String.format(
                    "Can't open report: directory <%s> doesn't exist.", reportDirectory
            ));
        }

        LOGGER.info("Starting web server for report directory <{}>", reportDirectory);
        Server server = setUpServer();
        server.start();

        openBrowser(server.getURI());
        LOGGER.info("Server started at <{}>. Press <Ctrl+C> to exit ...", server.getURI());
        server.join();
    }

    /**
     * Open the given url in default system browser.
     */
    private void openBrowser(URI url) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(url);
        } else {
            LOGGER.error("Can not open browser because this capability is not supported on " +
                    "your platform. You can use the link below to open the report manually.");
        }
    }

    /**
     * Set up server for report directory.
     */
    private Server setUpServer() {
        Server server = new Server(port);
        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setWelcomeFiles(new String[]{"index.html"});
        handler.setResourceBase(getReportDirectoryPath().toAbsolutePath().toString());
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
        server.setStopAtShutdown(true);
        server.setHandler(handlers);
        return server;
    }
}