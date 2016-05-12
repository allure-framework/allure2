package org.allurefw.report.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
@Command(name = "generate", description = "Generate report")
public class ReportGenerate extends ReportCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerate.class);

    public static final String MAIN = "org.allurefw.report.AllureMain";

    public static final String JAR_FILES = "*.jar";

    @Arguments(title = "Results directories", required = true,
            description = "A list of input directories to be processed")
    public List<String> results = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runUnsafe() throws Exception {
        validateResultsDirectories();
        CommandLine commandLine = createCommandLine();
        new DefaultExecutor().execute(commandLine);
        LOGGER.info("Report successfully generated to the directory <{}>. " +
                "Use `allure report open` command to show the report.", getReportDirectoryPath());
    }

    /**
     * Throws an exception if at least one results directory is missing.
     */
    protected void validateResultsDirectories() {
        for (String result : results) {
            if (Files.notExists(Paths.get(result))) {
                throw new AllureCommandException(String.format("Report directory <%s> not found.", result));
            }
        }
    }

    /**
     * Create a {@link CommandLine} to run bundle with needed arguments.
     */
    private CommandLine createCommandLine() throws IOException {
        return new CommandLine(getJavaExecutablePath())
                .addArguments(getBundleJavaOptsArgument())
                .addArgument(getLoggerConfigurationArgument())
                .addArgument("-jar")
                .addArgument(getExecutableJar())
                .addArguments(results.toArray(new String[results.size()]), false)
                .addArgument(getReportDirectoryPath().toString(), false);
    }

    /**
     * Returns the classpath for executable jar.
     */
    protected String getClasspath() throws IOException {
        List<String> classpath = new ArrayList<>();
        classpath.add(getBundleJarPath());
        classpath.addAll(getPluginsPath());
        return StringUtils.toString(classpath.toArray(new String[classpath.size()]), " ");
    }

    /**
     * Create an executable jar to generate the report. Created jar contains only
     * allure configuration file.
     */
    protected String getExecutableJar() throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN);
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, getClasspath());

        Path jar = createTempDirectory("exec").resolve("generate.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            output.putNextEntry(new JarEntry("allure.properties"));
            Path allureConfig = PROPERTIES.getAllureConfig();
            if (Files.exists(allureConfig)) {
                byte[] bytes = Files.readAllBytes(allureConfig);
                output.write(bytes);
            }
            output.closeEntry();
        }

        return jar.toAbsolutePath().toString();
    }

    /**
     * Returns the bundle jar classpath element.
     */
    protected String getBundleJarPath() throws MalformedURLException {
        Path path = PROPERTIES.getAllureHome().resolve("app/allure-bundle.jar").toAbsolutePath();
        if (Files.notExists(path)) {
            throw new AllureCommandException(String.format("Bundle not found by path <%s>", path));
        }
        return path.toUri().toURL().toString();
    }

    /**
     * Returns the plugins classpath elements.
     */
    protected List<String> getPluginsPath() throws IOException {
        List<String> result = new ArrayList<>();
        Path pluginsDirectory = PROPERTIES.getAllureHome().resolve("plugins").toAbsolutePath();
        if (Files.notExists(pluginsDirectory)) {
            return Collections.emptyList();
        }

        try (DirectoryStream<Path> plugins = Files.newDirectoryStream(pluginsDirectory, JAR_FILES)) {
            for (Path plugin : plugins) {
                result.add(plugin.toUri().toURL().toString());
            }
        }
        return result;
    }

    /**
     * Get argument to configure log level for bundle.
     */
    protected String getLoggerConfigurationArgument() {
        return String.format("-Dorg.slf4j.simpleLogger.defaultLogLevel=%s",
                isQuiet() || !isVerbose() ? "error" : "debug");
    }

    /**
     * Returns the bundle java options split by space.
     */
    protected String getBundleJavaOptsArgument() {
        return PROPERTIES.getBundleJavaOpts();
    }

    /**
     * Returns the path to java executable.
     */
    protected String getJavaExecutablePath() {
        String executableName = isWindows() ? "bin/java.exe" : "bin/java";
        return PROPERTIES.getJavaHome().resolve(executableName).toAbsolutePath().toString();
    }

    /**
     * Returns true if operation system is windows, false otherwise.
     */
    protected boolean isWindows() {
        return PROPERTIES.getOsName().contains("win");
    }
}
