package io.qameta.allure.utils;

import io.qameta.allure.Main;
import io.qameta.allure.command.AllureCommandException;
import io.qameta.allure.command.Context;
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

    public static void copyDirectory(Path source, Path dest) {
        if (Files.exists(source)) {
            try {
                Files.createDirectories(dest);
                Files.walkFileTree(source, new CopyVisitor(source, dest));
            } catch (IOException e) {
                throw new AllureCommandException("Could not copy directory");
            }
        }
    }

    public static Main createMain(Context context) {
        return new Main(
                context.getPluginsDirectory(),
                context.getEnabledPlugins()
        );
    }


    /**
     * Set up Jetty server to serve Allure Report
     */
    public static Server setUpServer(int port, Path reportDirectory) {
        Server server = new Server(port);
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
    public static void openBrowser(URI url) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(url);
        } else {
            LOGGER.error("Can not open browser because this capability is not supported on " +
                    "your platform. You can use the link below to open the report manually.");
        }
    }
}
