package io.qameta.allure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Artem Eroshenko eroshenkoam@qameta.io
 *         Date: 1/22/14
 */
public final class DummyReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyReportGenerator.class);

    DummyReportGenerator() {
    }

    /**
     * Generate Allure report data from directories with allure report results.
     *
     * @param args a list of directory paths. First (args.length - 1) arguments -
     *             results directories, last argument - the folder to generated data
     */
    public static void main(String[] args) {
        if (args.length < 2) { // NOSONAR
            LOGGER.error("There must be at least two arguments");
            return;
        }
        int lastIndex = args.length - 1;
        Path[] files = getFiles(args);

        String pluginsDirectory = System.getProperty("allure.pluginsDirectory");
        Main main = Objects.isNull(pluginsDirectory) ? new Main() : new Main(Paths.get(pluginsDirectory), null);
        main.generate(files[lastIndex], Arrays.copyOf(files, lastIndex));
    }

    public static Path[] getFiles(String[] paths) {
        return Arrays.stream(paths)
                .map(Paths::get)
                .toArray(Path[]::new);
    }
}
