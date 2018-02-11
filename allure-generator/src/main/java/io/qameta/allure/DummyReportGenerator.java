package io.qameta.allure;

import io.qameta.allure.plugin.DefaultPluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Artem Eroshenko eroshenkoam@qameta.io
 * Date: 1/22/14
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class DummyReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyReportGenerator.class);
    private static final int MIN_ARGUMENTS_COUNT = 2;

    private DummyReportGenerator() {
        throw new IllegalStateException("Do not instance");
    }

    /**
     * Generate Allure report data from directories with allure report results.
     *
     * @param args a list of directory paths. First (args.length - 1) arguments -
     *             results directories, last argument - the folder to generated data
     */
    public static void main(final String... args) throws IOException, InterruptedException {
        if (args.length < MIN_ARGUMENTS_COUNT) {
            LOGGER.error("There must be at least two arguments");
            return;
        }
        int lastIndex = args.length - 1;
        final Path[] files = getFiles(args);
        final List<Plugin> plugins = loadPlugins();
        LOGGER.info("Found {} plugins", plugins.size());
        plugins.forEach(plugin -> LOGGER.info(plugin.getConfig().getName()));
        final ReportGenerator generator = new ReportGenerator();
        generator.generate(files[lastIndex], Arrays.copyOf(files, lastIndex));
    }

    public static Path[] getFiles(final String... paths) {
        return Arrays.stream(paths)
                .map(Paths::get)
                .toArray(Path[]::new);
    }

    public static List<Plugin> loadPlugins() throws IOException {
        final Optional<Path> optional = Optional.ofNullable(System.getProperty("allure.plugins.directory"))
                .map(Paths::get)
                .filter(Files::isDirectory);
        if (!optional.isPresent()) {
            return Collections.emptyList();
        }
        final Path pluginsDirectory = optional.get();
        LOGGER.info("Found plugins directory {}", pluginsDirectory);
        final DefaultPluginLoader loader = new DefaultPluginLoader();
        final ClassLoader classLoader = DummyReportGenerator.class.getClassLoader();
        return Files.list(pluginsDirectory)
                .filter(Files::isDirectory)
                .map(pluginDir -> loader.loadPlugin(classLoader, pluginDir))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
