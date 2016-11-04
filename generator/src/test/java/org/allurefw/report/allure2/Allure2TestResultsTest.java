package org.allurefw.report.allure2;

import com.google.common.reflect.ClassPath;
import org.allurefw.report.Main;
import org.allurefw.report.ReportInfo;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.matchers.nio.PathMatchers.contains;
import static ru.yandex.qatools.matchers.nio.PathMatchers.hasFilesCount;
import static ru.yandex.qatools.matchers.nio.PathMatchers.isDirectory;

/**
 * @author charlie (Dmitry Baev).
 */
public class Allure2TestResultsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldCreateReportInfo() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, Collections.emptySet());

        ReportInfo report = main.createReport(getDataFolder("allure2data/"));

        Assert.assertThat(report, notNullValue());
        Assert.assertThat(report.getResults(), hasSize(7));
    }

    @Test
    public void shouldGenerateReport() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, null);
        Path output = folder.newFolder().toPath();
        main.generate(output, getDataFolder("allure2data/"));
        assertThat(output, contains("index.html"));
        assertThat(output, contains("data"));
        Path data = output.resolve("data");
        assertThat(data, isDirectory());

        assertThat(data, contains("test-cases"));
        Path testCases = data.resolve("test-cases");
        assertThat(testCases, isDirectory());
        assertThat(testCases, hasFilesCount(7, "*.json"));

        assertThat(data, contains("defects.json"));
        assertThat(data, contains("graph.json"));
        assertThat(data, contains("history.json"));
        assertThat(data, contains("timeline.json"));
        assertThat(data, contains("widgets.json"));
        assertThat(data, contains("xunit.json"));
        assertThat(data, contains("history.json"));
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