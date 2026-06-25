/*
 *  Copyright 2016-2026 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure;

import io.qameta.allure.option.ConfigOptions;
import io.qameta.allure.option.ReportLanguageOptions;
import io.qameta.allure.option.ReportNameOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class CommandsTest {

    /**
     * Verifies listing plugins succeeds even when the selected profile has no config file.
     * The test checks the command returns the no-error exit code.
     */
    @Description
    @Test
    void shouldNotFailWhenListPluginsWithoutConfig(@TempDir final Path home) {
        final Commands commands = new Commands(home);
        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("some-profile");
        final ExitCode exitCode = commands.listPlugins(options);

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    /**
     * Verifies report generation refuses to overwrite a non-empty report directory by default.
     * The test checks the command returns a generic error when the output directory already has content.
     */
    @Description
    @Test
    void shouldFailIfDirectoryExists(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));
        final Path reportPath = Files.createDirectories(temp.resolve("report"));
        Files.createTempFile(reportPath, "some", ".txt");
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.generate(
                reportPath, Collections.emptyList(), false, false,
                new ConfigOptions(), new ReportNameOptions(), new ReportLanguageOptions()
        );

        assertThat(exitCode)
                .isEqualTo(ExitCode.GENERIC_ERROR);
    }

    /**
     * Verifies plugin listing reads the selected commandline config profile.
     * The test checks a valid profile-backed config lets the command finish successfully.
     */
    @Description
    @Test
    void shouldListPlugins(@TempDir final Path home) throws Exception {
        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.listPlugins(options);

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    /**
     * Verifies commandline configuration loading from the selected profile.
     * The test checks the expected plugin list is parsed from the profile config file.
     */
    @Description
    @Test
    void shouldLoadConfig(@TempDir final Path home) throws Exception {
        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");

        final Commands commands = new Commands(home);
        final CommandlineConfig config = commands.getConfig(options);
        Allure.addAttachment(
                "loaded-config.txt",
                "text/plain",
                String.format("profile=test%nplugins=%s%n", config.getPlugins()),
                ".txt"
        );
        assertThat(config)
                .isNotNull();

        assertThat(config.getPlugins())
                .hasSize(3)
                .containsExactly("a", "b", "c");
    }

    /**
     * Verifies report generation allows an empty existing output directory.
     * The test checks generation exits successfully when there are no input results and no existing report files.
     */
    @Description
    @Test
    void shouldAllowEmptyReportDirectory(@TempDir final Path temp) throws Exception {
        final Path home = Files.createDirectories(temp.resolve("home"));

        createConfig(home, "allure-test.yml");

        final ConfigOptions options = mock(ConfigOptions.class);
        when(options.getProfile()).thenReturn("test");
        final Path reportPath = Files.createDirectories(temp.resolve("report"));
        final Commands commands = new Commands(home);
        final ExitCode exitCode = commands.generate(
                reportPath,
                Collections.emptyList(), false, false, options,
                new ReportNameOptions(), new ReportLanguageOptions()
        );

        assertThat(exitCode)
                .isEqualTo(ExitCode.NO_ERROR);
    }

    private void createConfig(final Path home, final String fileName) throws IOException {
        final Path configFolder = Files.createDirectories(home.resolve("config"));
        final Path config = configFolder.resolve(fileName);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            Files.copy(Objects.requireNonNull(is), config);
        }
    }
}
