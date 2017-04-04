package io.qameta.allure;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

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
        final ExitCode exitCode = commands.listPlugins("some-profile");

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    @Test
    public void shouldListPlugins() throws Exception {
        final Path home = folder.newFolder().toPath();
        createConfig(home, "allure-test.yml");

        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.listPlugins("test");

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    @Test
    public void shouldLoadConfig() throws Exception {
        final Path home = folder.newFolder().toPath();
        createConfig(home, "allure-test.yml");

        final Commands commands = new Commands(home);
        final CommandlineConfig config = commands.getConfig("test");
        assertThat(config)
                .isNotNull();

        assertThat(config.getPlugins())
                .hasSize(3)
                .containsExactly("a", "b", "c");
    }

    private void createConfig(final Path home, final String fileName) throws IOException {
        final Path configFolder = Files.createDirectories(home.resolve("config"));
        final Path config = configFolder.resolve(fileName);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            Files.copy(is, config);
        }
    }
}