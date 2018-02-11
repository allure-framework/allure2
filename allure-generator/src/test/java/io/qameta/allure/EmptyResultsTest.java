package io.qameta.allure;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

/**
 * @author charlie (Dmitry Baev).
 */
public class EmptyResultsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldAllowEmptyResultsDirectory() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath();
        final Path outputDirectory = folder.newFolder().toPath();
        final ReportGenerator generator = new ReportGenerator();
        generator.generate(outputDirectory, resultsDirectory);
    }

    @Test
    public void shouldAllowNonExistsResultsDirectory() throws Exception {
        final Path resultsDirectory = folder.newFolder().toPath().resolve("some-dir");
        final Path outputDirectory = folder.newFolder().toPath();
        final ReportGenerator generator = new ReportGenerator();
        generator.generate(outputDirectory, resultsDirectory);
    }

    @Test
    public void shouldAllowRegularFileAsResultsDirectory() throws Exception {
        final Path resultsDirectory = folder.newFile().toPath();
        final Path outputDirectory = folder.newFolder().toPath();
        final ReportGenerator generator = new ReportGenerator();

        generator.generate(outputDirectory, resultsDirectory);
    }
}
