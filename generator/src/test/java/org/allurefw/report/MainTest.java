package org.allurefw.report;

import com.google.common.reflect.ClassPath;
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

/**
 * @author charlie (Dmitry Baev).
 */
public class MainTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldCreateReportInfo() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Path work = folder.newFolder().toPath();
        Main main = new Main(plugins, work, Collections.emptySet());

        ReportInfo report = main.createReport(getDataFolder("allure1data/"));

        assertThat(report, notNullValue());
        assertThat(report.getResults(), hasSize(20));
    }

    @Test
    public void shouldGenerateTheReport() throws Exception {
        Path plugins = folder.newFolder().toPath();
        Path work = folder.newFolder().toPath();
        Main main = new Main(plugins, work, Collections.emptySet());
        main.generate(folder.newFolder().toPath(), getDataFolder("allure1data/"));
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