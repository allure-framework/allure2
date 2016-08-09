package org.allurefw.report.utils;

import org.allurefw.report.command.AllureCommandException;
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
 * @author charlie (Dmitry Baev).
 */
public final class CommandUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class);

    CommandUtils() {
    }

    public static void validateDirectoryExists(Path directory) {
        if (Files.notExists(directory)) {
            throw new AllureCommandException(String.format("Directory <%s> not found.", directory));
        }
    }

    /**
     * Set up Jetty server to serve Allure Report
     */
    public static Server setUpServer(int port, Path reportDirectory) {
        Server server = new Server(port);
        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setWelcomeFiles(new String[]{"index.html"});
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
    public static void openBrowser(URI url) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(url);
        } else {
            LOGGER.error("Can not open browser because this capability is not supported on " +
                    "your platform. You can use the link below to open the report manually.");
        }
    }
}
