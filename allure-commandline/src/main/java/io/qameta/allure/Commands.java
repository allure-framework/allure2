/*
 *  Copyright 2016-2024 Qameta Software Inc
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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.qameta.allure.config.ConfigLoader;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.option.ConfigOptions;
import io.qameta.allure.option.ReportLanguageOptions;
import io.qameta.allure.option.ReportNameOptions;
import io.qameta.allure.plugin.DefaultPluginLoader;
import io.qameta.allure.util.DeleteVisitor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.AWTError;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.qameta.allure.DefaultResultsVisitor.probeContentType;
import static java.lang.String.format;

/**
 * The type Commands.
 *
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "ReturnCount"})
public class Commands {

    private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);
    private static final String DIRECTORY_EXISTS_MESSAGE = "Allure: Target directory {} for the report is already"
                                                           + " in use, add a '--clean' option to overwrite";

    private final Path allureHome;

    /**
     * Instantiates a new Commands.
     *
     * @param allureHome the allure home
     */
    public Commands(final Path allureHome) {
        this.allureHome = allureHome;
    }

    /**
     * Gets config.
     *
     * @param configOptions the config options
     * @return the config
     */
    public CommandlineConfig getConfig(final ConfigOptions configOptions) {
        return getConfigFile(configOptions)
                .map(ConfigLoader::new)
                .map(ConfigLoader::load)
                .orElseGet(CommandlineConfig::new);
    }

    /**
     * Gets config file.
     *
     * @param configOptions the config options
     * @return the config file
     */
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

    /**
     * Gets config file name.
     *
     * @param profile the profile
     * @return the config file name
     */
    public String getConfigFileName(final String profile) {
        return Objects.isNull(profile)
                ? "allure.yml"
                : format("allure-%s.yml", profile);
    }

    /**
     * Generate exit code.
     *
     * @param reportDirectory    the report directory
     * @param resultsDirectories the results directories
     * @param clean              the clean
     * @param singleFileMode     the single file mode
     * @param configuration      the configuration
     * @return the exit code
     */
    private ExitCode generate(final Path reportDirectory,
                              final List<Path> resultsDirectories,
                              final boolean clean,
                              final boolean singleFileMode,
                              final Configuration configuration) {
        final boolean directoryExists = Files.exists(reportDirectory);
        if (clean && directoryExists) {
            FileUtils.deleteQuietly(reportDirectory.toFile());
        } else if (directoryExists && isDirectoryNotEmpty(reportDirectory)) {
            LOGGER.error(DIRECTORY_EXISTS_MESSAGE, reportDirectory.toAbsolutePath());
            return ExitCode.GENERIC_ERROR;
        }
        final ReportGenerator generator = new ReportGenerator(configuration);
        if (singleFileMode) {
            generator.generateSingleFile(reportDirectory, resultsDirectories);
        } else {
            generator.generate(reportDirectory, resultsDirectories);
        }
        LOGGER.info("Report successfully generated to {}", reportDirectory);
        return ExitCode.NO_ERROR;
    }

    /**
     * Generate exit code.
     *
     * @param reportDirectory    the report directory
     * @param resultsDirectories the results directories
     * @param clean              the clean
     * @param singleFileMode     the single file mode
     * @param profile            the profile
     * @param reportNameOptions  the report name options
     * @return the exit code
     */
    public ExitCode generate(final Path reportDirectory,
                             final List<Path> resultsDirectories,
                             final boolean clean,
                             final boolean singleFileMode,
                             final ConfigOptions profile,
                             final ReportNameOptions reportNameOptions,
                             final ReportLanguageOptions reportLanguageOptions) {
        final Configuration configuration = createReportConfiguration(
                profile, reportNameOptions, reportLanguageOptions
        );

        return generate(reportDirectory, resultsDirectories, clean, singleFileMode, configuration);
    }

    /**
     * Serve exit code.
     *
     * @param resultsDirectories    the results directories
     * @param host                  the host
     * @param port                  the port
     * @param configOptions         the config options
     * @param reportNameOptions     the report name options
     * @param reportLanguageOptions the report language options
     * @return the exit code
     */
    public ExitCode serve(final List<Path> resultsDirectories,
                          final String host,
                          final int port,
                          final ConfigOptions configOptions,
                          final ReportNameOptions reportNameOptions,
                          final ReportLanguageOptions reportLanguageOptions) {
        LOGGER.info("Generating report to temp directory...");

        final Path reportDirectory;
        try {
            final Path tmp = Files.createTempDirectory("");
            reportDirectory = tmp.resolve("allure-report");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LOGGER.info("Shutting down...");
                    Files.walkFileTree(tmp, new DeleteVisitor());
                } catch (IOException ignored) {
                    // do nothing
                }
            }));
        } catch (IOException e) {
            LOGGER.error("Could not create temp directory", e);
            return ExitCode.GENERIC_ERROR;
        }

        final Configuration configuration = createReportConfiguration(
                configOptions, reportNameOptions, reportLanguageOptions
        );

        final ExitCode exitCode = generate(
                reportDirectory,
                resultsDirectories,
                false,
                false,
                configuration
        );
        if (exitCode.isSuccess()) {
            return open(reportDirectory, host, port);
        }
        return exitCode;
    }

    /**
     * Open exit code.
     *
     * @param reportDirectory the report directory
     * @param host            the host
     * @param port            the port
     * @return the exit code
     */
    public ExitCode open(final Path reportDirectory, final String host, final int port) {
        LOGGER.info("Starting web server...");
        final HttpServer server;
        try {
            server = setUpServer(host, port, reportDirectory);
            server.start();
        } catch (Exception e) {
            LOGGER.error("Could not serve the report", e);
            return ExitCode.GENERIC_ERROR;
        }

        final InetSocketAddress socketAddress = server.getAddress();
        final URI uri = URI.create("http://"
                                   + socketAddress.getHostString()
                                   + ":"
                                   + socketAddress.getPort()
        );

        try {
            openBrowser(uri);
        } catch (IOException | AWTError e) {
            LOGGER.error(
                    "Could not open the report in browser, try to open it manually {}",
                    uri,
                    e
            );
        }
        LOGGER.info("Server started at <{}>. Press <Ctrl+C> to exit", uri);
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.error("Report serve interrupted", e);
            return ExitCode.GENERIC_ERROR;
        }
        return ExitCode.NO_ERROR;
    }

    /**
     * List plugins exit code.
     *
     * @param configOptions the config options
     * @return the exit code
     */
    public ExitCode listPlugins(final ConfigOptions configOptions) {
        final CommandlineConfig config = getConfig(configOptions);
        config.getPlugins().forEach(System.out::println);
        return ExitCode.NO_ERROR;
    }

    /**
     * Creates report configuration for a given profile.
     *
     * @param profile               selected profile.
     * @param reportNameOptions     the report name options
     * @param reportLanguageOptions the report language options
     * @return created report configuration.
     */
    protected Configuration createReportConfiguration(
            final ConfigOptions profile,
            final ReportNameOptions reportNameOptions,
            final ReportLanguageOptions reportLanguageOptions) {
        final DefaultPluginLoader loader = new DefaultPluginLoader();
        final CommandlineConfig commandlineConfig = getConfig(profile);
        final ClassLoader classLoader = getClass().getClassLoader();
        final List<Plugin> plugins = commandlineConfig.getPlugins().stream()
                .map(name -> loader.loadPlugin(classLoader, allureHome.resolve("plugins").resolve(name)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return ConfigurationBuilder
                .bundled()
                .withPlugins(plugins)
                .withReportName(reportNameOptions.getReportName())
                .withReportLanguage(reportLanguageOptions.getReportLanguage())
                .build();
    }

    /**
     * Set up HttpServer to serve Allure Report.
     *
     * @param host            the host
     * @param port            the port
     * @param reportDirectory the report directory
     * @return self for method chaining
     * @throws IOException the io exception
     */
    protected HttpServer setUpServer(final String host, final int port, final Path reportDirectory) throws IOException {
        final HttpServer server = HttpServer
                .create(new InetSocketAddress(Objects.isNull(host) ? "localhost" : host, port), 0);

        server.createContext("/", exchange -> {
            final Path resolve = reportDirectory.resolve("." + exchange.getRequestURI().getPath());
            if (Files.isDirectory(resolve)) {
                serveFile(exchange, resolve.resolve("index.html"));
            } else {
                serveFile(exchange, resolve);
            }
        });

        return server;
    }

    private static void serveFile(final HttpExchange exchange, final Path resolve) throws IOException {
        if (Files.isRegularFile(resolve)) {
            final String contentType = probeContentType(resolve);
            exchange.sendResponseHeaders(200, Files.size(resolve));
            exchange.getResponseHeaders().add("Content-Type", contentType);
            try (OutputStream os = exchange.getResponseBody()) {
                Files.copy(resolve, os);
            }
        } else {
            final String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * Open the given url in default system browser.
     *
     * @param url the url
     * @throws IOException the io exception
     */
    protected void openBrowser(final URI url) throws IOException {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(url);
            } catch (UnsupportedOperationException e) {
                LOGGER.error("Browse operation is not supported on your platform."
                             + "You can use the link below to open the report manually.", e);
            }
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
