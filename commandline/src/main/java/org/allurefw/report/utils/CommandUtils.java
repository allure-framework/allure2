package org.allurefw.report.utils;

import org.allurefw.report.CommandProperties;
import org.allurefw.report.Main;
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
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public final class CommandUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class);

    CommandUtils() {
    }

    public static void copyWeb(CommandProperties properties, Path outputDirectory) {
        Optional.ofNullable(properties.getAllureHome())
                .map(path -> path.resolve("web"))
                .filter(Files::exists)
                .ifPresent(path -> copyDirectory(path, outputDirectory));
    }

    public static void copyWeb(Path webDirectory, Path outputDirectory) {
        if (Files.exists(webDirectory)) {
            copyDirectory(webDirectory, outputDirectory);
        }
    }

    public static void copyDirectory(Path source, Path dest) {
        try {
            Files.createDirectories(dest);
            Files.walkFileTree(source, new CopyVisitor(source, dest));
        } catch (IOException e) {
            throw new AllureCommandException("Could not copy directory");
        }
    }

    public static Main createMain(CommandProperties properties, Path workDirectory) {
        Optional<Path> pluginsDirectory = Optional.ofNullable(properties.getAllureHome())
                .map(path -> path.resolve("plugins"))
                .filter(Files::exists);
        return pluginsDirectory.isPresent()
                ? new Main(pluginsDirectory.get(), workDirectory, Collections.emptySet())
                : new Main();
    }

    public static Main createMain(Path pluginsDirectory, Path workDirectory) {
        return Files.exists(pluginsDirectory)
                ? new Main(pluginsDirectory, workDirectory, Collections.emptySet())
                : new Main();
    }

    public static Main createMain(Path pluginsDirectory, Path workDirectory, Set<String> enabledPlugins) {
        return Files.exists(pluginsDirectory)
                ? new Main(pluginsDirectory, workDirectory, enabledPlugins)
                : new Main();
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
