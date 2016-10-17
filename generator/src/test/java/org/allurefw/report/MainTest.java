package org.allurefw.report;

import com.google.common.reflect.ClassPath;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
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

        ReportInfo report = main.createReport(getDataFolder("allure1data/"));

        assertThat(report, notNullValue());
        assertThat(report.getResults(), hasSize(20));
    }

    @Test
    public void shouldGenerateTheReport() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, null);
        Path output = folder.newFolder().toPath();
        main.generate(output, getDataFolder("allure1data/"));
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
    }

    @Test
    public void shouldUnpackPluginFiles() throws Exception {
        String name = "some-name";
        Path dir = getPluginDirectory();
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

    private Path getPluginDirectory() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dummy-plugin.zip")) {
            Path pluginsDirectory = folder.newFolder().toPath();
            ZipUtil.unpack(is, pluginsDirectory.toFile());
            return pluginsDirectory;
        }
    }

    private Path getDataFolder(String prefix) throws IOException {
        ClassPath classPath = ClassPath.from(getClass().getClassLoader());
        Map<String, URL> files = classPath.getResources().stream()
                .filter(info -> info.getResourceName().startsWith(prefix))
                .collect(Collectors.toMap(
                        info -> info.getResourceName().substring(prefix.length()),
                        ClassPath.ResourceInfo::url)
                );
        Path dir = folder.newFolder().toPath();
        files.forEach((name, url) -> {
            Path file = dir.resolve(name);
            try (InputStream is = url.openStream()) {
                Files.copy(is, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return dir;
    }
}