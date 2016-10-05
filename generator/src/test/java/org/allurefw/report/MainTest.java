package org.allurefw.report;

import com.google.common.reflect.ClassPath;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.yandex.qatools.matchers.nio.PathMatchers;

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

    @Test
    public void shouldUnpackPluginFiles() throws Exception {
        String name = "some-name";
        Path archive = getPluginArchive();
        PluginDescriptor dummy = new PluginDescriptor();
        dummy.setName(name);
        Plugin plugin = new Plugin(dummy, null, archive, true);
        Path output = folder.newFolder().toPath();
        Optional<String> pluginName = Main.copyPluginStatic(plugin, output);
        assertThat(pluginName, isPresent());
        assertThat(pluginName, hasValue(name));

        assertThat(output, contains(name));
        Path pluginDirectory = output.resolve(name);
        assertThat(pluginDirectory, isDirectory());
        assertThat(pluginDirectory, contains("index.js"));
    }

    private Path getPluginArchive() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dummy-plugin.zip")) {
            Path archive = folder.newFolder().toPath().resolve("dummy-plugin.zip");
            Files.copy(is, archive);
            return archive;
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