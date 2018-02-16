package io.qameta.allure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
        final ReportGenerator generator = new ReportGenerator();
        generator.generate(files[lastIndex], Arrays.copyOf(files, lastIndex));
        System.exit(0);
    }

    public static Path[] getFiles(final String... paths) {
        return Arrays.stream(paths)
                .map(Paths::get)
                .toArray(Path[]::new);
    }
}
