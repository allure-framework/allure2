/*
 *  Copyright 2019 Qameta Software OÜ
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
import io.qameta.allure.config.ConfigLoader;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.option.ConfigOptions;
import io.qameta.allure.plugin.DefaultPluginLoader;
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
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "ReturnCount"})
public class Commands {

    private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);
    private static final String DIRECTORY_EXISTS_MESSAGE = "Allure: Target directory %s for the report is already"
            + " in use, add a '--clean' option to overwrite";

    private final Path allureHome;

    public Commands(final Path allureHome) {
        this.allureHome = allureHome;
    }

    public CommandlineConfig getConfig(final ConfigOptions configOptions) throws IOException {
        return getConfigFile(configOptions)
                .map(ConfigLoader::new)
                .map(ConfigLoader::load)
                .orElseGet(CommandlineConfig::new);
    }

    public Optional<Path> getConfigFile(final ConfigOptions configOptions) {
        if (Objects.nonNull(configOptions.getConfigPath())) {
            return Optional.of(Paths.get(configOptions.getConfigPath()));
        }
        if (Objects.nonNull(configOptions.getConfigDirectory())) {
            return Optional.of(Paths.get(configOptions.getConfigDirectory())
                    .resolve(getConfigFileName(configOptions.getProfile())));
        }
        if (Objects.nonNull(allureHome)) {
            return Optional.of(allureHome.resolve("config")
                    .resolve(getConfigFileName(configOptions.getProfile())));
        }
        return Optional.empty();
    }

    public String getConfigFileName(final String profile) {
        return Objects.isNull(profile)
                ? "allure.yml"
                : format("allure-%s.yml", profile);
    }

    public ExitCode generate(final Path reportDirectory,
                             final List<Path> resultsDirectories,
                             final boolean clean,
                             final ConfigOptions profile) {
        final boolean directoryExists = Files.exists(reportDirectory);
        if (clean && directoryExists) {
            FileUtils.deleteQuietly(reportDirectory.toFile());
        } else if (directoryExists && isDirectoryNotEmpty(reportDirectory)) {
            JCommander.getConsole().println(format(DIRECTORY_EXISTS_MESSAGE, reportDirectory.toAbsolutePath()));
            return ExitCode.GENERIC_ERROR;
        }
        try {
            final ReportGenerator generator = new ReportGenerator(createReportConfiguration(profile));
            generator.generate(reportDirectory, resultsDirectories);
        } catch (IOException e) {
            LOGGER.error("Could not generate report: {}", e);
            return ExitCode.GENERIC_ERROR;
        }
        LOGGER.info("Report successfully generated to {}", reportDirectory);
        return ExitCode.NO_ERROR;
    }

    public ExitCode serve(final List<Path> resultsDirectories,
                          final String host,
                          final int port,
                          final ConfigOptions configOptions) {
        LOGGER.info("Generating report to temp directory...");

        final Path reportDirectory;
        try {
            final Path tmp = Files.createTempDirectory("");
            reportDirectory = tmp.resolve("allure-report");
            tmp.toFile().deleteOnExit();
        } catch (IOException e) {
            LOGGER.error("Could not create temp directory: {}", e);
            return ExitCode.GENERIC_ERROR;
        }

        final ExitCode exitCode = generate(
                reportDirectory,
                resultsDirectories,
                false,
                configOptions
        );
        if (exitCode.isSuccess()) {
            return open(reportDirectory, host, port);
        }
        return exitCode;
    }

    public ExitCode open(final Path reportDirectory, final String host, final int port) {
        LOGGER.info("Starting web server...");
        final Server server = setUpServer(host, port, reportDirectory);
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

    public ExitCode listPlugins(final ConfigOptions configOptions) {
        try {
            final CommandlineConfig config = getConfig(configOptions);
            config.getPlugins().forEach(LOGGER::info);
        } catch (IOException e) {
            LOGGER.error("Can't read config: {}", e);
            return ExitCode.GENERIC_ERROR;
        }
        return ExitCode.NO_ERROR;
    }

    /**
     * Creates report configuration for a given profile.
     *
     * @param profile selected profile.
     * @return created report configuration.
     * @throws IOException if any occurs.
     */
    protected Configuration createReportConfiguration(final ConfigOptions profile) throws IOException {
        final DefaultPluginLoader loader = new DefaultPluginLoader();
        final CommandlineConfig commandlineConfig = getConfig(profile);
        final ClassLoader classLoader = getClass().getClassLoader();
        final List<Plugin> plugins = commandlineConfig.getPlugins().stream()
                .map(name -> loader.loadPlugin(classLoader, allureHome.resolve("plugins").resolve(name)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new ConfigurationBuilder()
                .useDefault()
                .fromPlugins(plugins)
                .build();
    }

    /**
     * Set up Jetty server to serve Allure Report.
     */
    protected Server setUpServer(final String host, final int port, final Path reportDirectory) {
        final Server server = Objects.isNull(host)
                ? new Server(port)
                : new Server(new InetSocketAddress(host, port));
        final ResourceHandler handler = new ResourceHandler();
        handler.setRedirectWelcome(true);
        handler.setDirectoriesListed(true);
        handler.setResourceBase(reportDirectory.toAbsolutePath().toString());
        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
        server.setStopAtShutdown(true);
        server.setHandler(handlers);
        return server;
    }

    /**
     * Open the given url in default system browser.
     */
    protected void openBrowser(final URI url) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(url);
        } else {
            LOGGER.error("Can not open browser because this capability is not supported on "
                    + "your platform. You can use the link below to open the report manually.");
        }
    }

    private boolean isDirectoryNotEmpty(final Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            return stream.iterator().hasNext();
        } catch (IOException e) {
            LOGGER.warn("Could not scan report directory {}", path, e);
            return false;
        }
    }
}
