package io.qameta.allure;

import io.qameta.allure.option.ConfigOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
public class CommandsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldNotFailWhenListPluginsWithoutConfig() throws Exception {
        final Path home = folder.newFolder().toPath();
        final Commands commands = new Commands(home);
        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("some-profile");
        final ExitCode exitCode = commands.listPlugins(options);

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    @Test
    public void shouldFailIfDirectoryExists() throws Exception {
        final Path home = folder.newFolder().toPath();
        final Path reportPath = folder.newFolder().toPath();
        Files.createTempFile(reportPath, "some", ".txt");
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.generate(reportPath, null, false,
                null);

        assertThat(exitCode)
                .isEqualTo(ExitCode.GENERIC_ERROR);
    }

    @Test
    public void shouldListPlugins() throws Exception {
        final Path home = folder.newFolder().toPath();
        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.listPlugins(options);

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    @Test
    public void shouldLoadConfig() throws Exception {
        final Path home = folder.newFolder().toPath();
        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");

        final Commands commands = new Commands(home);
        final CommandlineConfig config = commands.getConfig(options);
        assertThat(config)
                .isNotNull();

        assertThat(config.getPlugins())
                .hasSize(3)
                .containsExactly("a", "b", "c");
    }

    @Test
    public void shouldAllowEmptyReportDirectory() throws Exception {
        final Path home = folder.newFolder().toPath();

        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");
        final Path reportPath = folder.newFolder().toPath();
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.generate(reportPath, Collections.emptyList(), false, options);

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    private void createConfig(final Path home, final String fileName) throws IOException {
        final Path configFolder = Files.createDirectories(home.resolve("config"));
        final Path config = configFolder.resolve(fileName);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            Files.copy(is, config);
        }
    }
}