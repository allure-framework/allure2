package org.allurefw.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
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
        ReportGenerator reportGenerator = new ReportGenerator(Arrays.copyOf(files, lastIndex));
        reportGenerator.generate(files[lastIndex]);
    }

    public static Path[] getFiles(String[] paths) {
        return Arrays.stream(paths)
                .map(Paths::get)
                .toArray(Path[]::new);
    }
}
