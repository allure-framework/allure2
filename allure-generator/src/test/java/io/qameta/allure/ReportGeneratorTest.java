package io.qameta.allure;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static io.qameta.allure.testdata.TestData.unpackDummyResources;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.matchers.nio.PathMatchers.contains;
import static ru.yandex.qatools.matchers.nio.PathMatchers.hasFilesCount;
import static ru.yandex.qatools.matchers.nio.PathMatchers.isDirectory;

/**
 * @author charlie (Dmitry Baev).
 */
public class ReportGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldGenerateReport() throws Exception {
        ReportGenerator generator = new ReportGenerator(new DefaultConfiguration());
        Path output = folder.newFolder().toPath();
        Path resultsDirectory = folder.newFolder().toPath();
        unpackDummyResources("allure1data/", resultsDirectory);
        generator.generate(output, resultsDirectory);
        assertThat(output, contains("index.html"));
        assertThat(output, contains("data"));
        Path data = output.resolve("data");
        assertThat(data, isDirectory());

        assertThat(data, contains("test-cases"));
        Path testCases = data.resolve("test-cases");
        assertThat(testCases, isDirectory());
        assertThat(testCases, hasFilesCount(20, "*.json"));

        assertThat(data, contains("attachments"));
        Path attachments = data.resolve("attachments");
        assertThat(attachments, isDirectory());
        assertThat(attachments, hasFilesCount(13));

        assertThat(data, contains("categories.json"));
        assertThat(data, contains("graph.json"));
        assertThat(data, contains("timeline.json"));
        assertThat(data, contains("widgets.json"));
        assertThat(data, contains("xunit.json"));

        assertThat(output, contains("history"));
        Path history = output.resolve("history");
        assertThat(history, contains("history.json"));

        assertThat(output, contains("export"));
        Path export = output.resolve("export");
        assertThat(export, contains("mail.html"));
    }
}