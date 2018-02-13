package io.qameta.allure;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author charlie (Dmitry Baev).
 */
public class MainTest {

    @Test
    public void shouldGenerate() throws IOException, InterruptedException {
        final ReportGenerator generator = new ReportGenerator();
        final Path output = Paths.get("/Users/charlie/projects/allure2/allure-generator/build/test-report");
        Files.createDirectories(output);
        final Path resultsDirectory = Paths.get("/Users/charlie/projects/allure2/allure-generator/test-data/demo");
        generator.generate(output, resultsDirectory);
    }
}
