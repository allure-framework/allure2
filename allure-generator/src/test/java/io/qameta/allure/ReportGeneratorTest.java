package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.qameta.allure.testdata.TestData.unpackDummyResources;
import static org.assertj.core.api.Assertions.assertThat;

public class ReportGeneratorTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    private static Path output;

    @BeforeClass
    public static void setUp() throws Exception {
        final Configuration configuration = new ConfigurationBuilder().useDefault().build();
        final ReportGenerator generator = new ReportGenerator(configuration);
        output = folder.newFolder().toPath();
        final Path resultsDirectory = folder.newFolder().toPath();
        unpackDummyResources("allure1data/", resultsDirectory);
        generator.generate(output, resultsDirectory);
    }

    @Test
    public void shouldGenerateIndexHtml() throws Exception {
        assertThat(output.resolve("index.html"))
                .isRegularFile();
    }

    @Test
    public void shouldWriteReportStatic() throws Exception {
        assertThat(output.resolve("app.js"))
                .isRegularFile();
        assertThat(output.resolve("styles.css"))
                .isRegularFile();
    }

    @Test
    public void shouldGenerateCategoriesJson() throws Exception {
        assertThat(output.resolve("data/categories.json"))
                .isRegularFile();
    }

    @Test
    public void shouldGenerateGraphJson() throws Exception {
        assertThat(output.resolve("data/graph.json"))
                .isRegularFile();
    }

    @Test
    public void shouldGenerateXunitJson() throws Exception {
        assertThat(output.resolve("data/suites.json"))
                .isRegularFile();
    }

    @Test
    public void shouldGenerateTimelineJson() throws Exception {
        assertThat(output.resolve("data/timeline.json"))
                .isRegularFile();
    }

    @Test
    public void shouldGenerateWidgetsJson() throws Exception {
        assertThat(output.resolve("data/widgets.json"))
                .isRegularFile();
    }

    @Test
    public void shouldGenerateAttachments() throws Exception {
        final Path attachmentsFolder = output.resolve("data/attachments");
        assertThat(attachmentsFolder)
                .isDirectory();
        assertThat(Files.list(attachmentsFolder))
                .hasSize(13);
    }

    @Test
    public void shouldGenerateTestCases() throws Exception {
        final Path testCasesFolder = output.resolve("data/test-cases");
        assertThat(testCasesFolder)
                .isDirectory();
        assertThat(Files.list(testCasesFolder))
                .hasSize(20);
    }

    @Test
    public void shouldGenerateHistory() throws Exception {
        assertThat(output.resolve("history/history.json"))
                .isRegularFile();
    }

    @Test
    public void shouldGenerateMail() throws Exception {
        assertThat(output.resolve("export/mail.html"))
                .isRegularFile();
    }


}