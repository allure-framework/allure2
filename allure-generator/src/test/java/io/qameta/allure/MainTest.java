package io.qameta.allure;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static io.qameta.allure.testdata.TestData.unpackDummyPlugin;
import static io.qameta.allure.testdata.TestData.unpackDummyResources;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.matchers.nio.PathMatchers.contains;
import static ru.yandex.qatools.matchers.nio.PathMatchers.hasFilesCount;
import static ru.yandex.qatools.matchers.nio.PathMatchers.isDirectory;

/**
 * @author charlie (Dmitry Baev).
 */
public class MainTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldCreateReportInfo() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, Collections.emptySet());

        Path resultsDirectory = folder.newFolder().toPath();
        unpackDummyResources("allure1data/", resultsDirectory);

        ReportInfo report = main.createReport(resultsDirectory);

        assertThat(report, notNullValue());
        assertThat(report.getResults(), hasSize(20));
    }

    @Test
    public void shouldGenerateReport() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, null);
        Path output = folder.newFolder().toPath();
        Path resultsDirectory = folder.newFolder().toPath();
        unpackDummyResources("allure1data/", resultsDirectory);
        main.generate(output, resultsDirectory);
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

        assertThat(data, contains("defects.json"));
        assertThat(data, contains("graph.json"));
        assertThat(data, contains("history.json"));
        assertThat(data, contains("timeline.json"));
        assertThat(data, contains("widgets.json"));
        assertThat(data, contains("xunit.json"));
        assertThat(data, contains("history.json"));
    }

    @Test
    public void shouldUnpackPluginFiles() throws Exception {
        String name = "some-name";

        Path dir = folder.newFolder().toPath();
        unpackDummyPlugin(dir);

        PluginDescriptor dummy = new PluginDescriptor();
        dummy.setName(name);
        Plugin plugin = new Plugin(dummy, null, dir, true);
        Path output = folder.newFolder().toPath();
        Optional<String> pluginName = Main.unpackStatic(plugin, output);
        assertThat(pluginName, isPresent());
        assertThat(pluginName, hasValue(name));

        assertThat(output, contains(name));
        Path pluginDirectory = output.resolve(name);
        assertThat(pluginDirectory, isDirectory());
        assertThat(pluginDirectory, contains("index.js"));
    }
}