/*
 *  Copyright 2016-2023 Qameta Software OÜ
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.qameta.allure.CommandLine.GENERATE_COMMAND;
import static io.qameta.allure.CommandLine.OPEN_COMMAND;
import static io.qameta.allure.CommandLine.PLUGIN_COMMAND;
import static io.qameta.allure.CommandLine.SERVE_COMMAND;
import static io.qameta.allure.ExitCode.ARGUMENT_PARSING_ERROR;
import static io.qameta.allure.ExitCode.NO_ERROR;
import static io.qameta.allure.testdata.TestData.randomPort;
import static io.qameta.allure.testdata.TestData.randomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
class CommandLineTest {

    private Commands commands;
    private CommandLine commandLine;

    @BeforeEach
    void setUp() {
        this.commands = mock(Commands.class);
        this.commandLine = new CommandLine(commands);
    }

    @Test
    void shouldParseEmptyArguments() {
        final Optional<ExitCode> parse = commandLine.parse();
        assertThat(parse)
                .hasValue(ExitCode.ARGUMENT_PARSING_ERROR);
    }

    @Test
    void shouldParseVerboseFlag() {
        final Optional<ExitCode> parse = commandLine
                .parse("-v", "--help");

        assertThat(parse)
                .isEmpty();

        assertThat(commandLine.getMainCommand().getVerboseOptions())
                .isNotNull()
                .hasFieldOrPropertyWithValue("verbose", true)
                .hasFieldOrPropertyWithValue("quiet", false);

        final ExitCode exitCode = commandLine.run();
        assertThat(exitCode)
                .isEqualTo(NO_ERROR);
    }

    @Test
    void shouldParseQuietFlag() {
        final Optional<ExitCode> parse = commandLine
                .parse("-q", "--help");

        assertThat(parse)
                .isEmpty();

        assertThat(commandLine.getMainCommand().getVerboseOptions())
                .isNotNull()
                .hasFieldOrPropertyWithValue("verbose", false)
                .hasFieldOrPropertyWithValue("quiet", true);

        final ExitCode exitCode = commandLine.run();
        assertThat(exitCode)
                .isEqualTo(NO_ERROR);
    }

    @Test
    void shouldAllowResultsDirectoriesThatNotExists() {
        final Optional<ExitCode> exitCode = commandLine.parse(GENERATE_COMMAND, randomString(), randomString());
        assertThat(exitCode)
                .isEmpty();
    }

    @Test
    void shouldRunGenerate(@TempDir final Path temp) throws IOException {
        final Path report = Files.createDirectories(temp.resolve("report"));
        final Path firstResult = Files.createDirectories(temp.resolve("first"));
        final Path secondResult = Files.createDirectories(temp.resolve("second"));
        final List<Path> results = Arrays.asList(firstResult, secondResult);

        when(commands.generate(eq(report), eq(results), eq(false), any(ConfigOptions.class)))
                .thenReturn(NO_ERROR);

        final Optional<ExitCode> exitCode = commandLine.parse(
                GENERATE_COMMAND, firstResult.toString(), secondResult.toString(),
                "--output", report.toString()
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode code = commandLine.run();
        verify(commands, times(1)).generate(eq(report), eq(results), eq(false), any(ConfigOptions.class));
        assertThat(code)
                .isEqualTo(NO_ERROR);
    }

    @Test
    void shouldRunOpen(@TempDir final Path report) {
        final int port = randomPort();
        when(commands.open(report, null, port))
                .thenReturn(NO_ERROR);

        final Optional<ExitCode> exitCode = commandLine.parse(
                OPEN_COMMAND, "--port", String.valueOf(port), report.toString()
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode code = commandLine.run();
        verify(commands, times(1)).open(report, null, port);
        assertThat(code)
                .isEqualTo(NO_ERROR);
    }

    @Test
    void shouldNotLetToSpecifyFewReportDirectories(@TempDir final Path temp) throws IOException {
        final Path first = Files.createDirectories(temp.resolve("first"));
        final Path second = Files.createDirectories(temp.resolve("second"));

        final Optional<ExitCode> exitCode = commandLine.parse(
                OPEN_COMMAND, first.toString(), second.toString()
        );
        assertThat(exitCode)
                .isPresent()
                .hasValue(ExitCode.ARGUMENT_PARSING_ERROR);
    }

    @Test
    void shouldRunHelpForCommand() {
        final Optional<ExitCode> exitCode = commandLine.parse(
                "--help", OPEN_COMMAND
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(NO_ERROR);
    }

    @Test
    void shouldPrintVersion() {
        final Optional<ExitCode> exitCode = commandLine.parse(
                "--version"
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(NO_ERROR);
    }

    @Test
    void shouldParseServeCommand(@TempDir final Path temp) throws IOException {
        final int port = randomPort();
        final String host = randomString();
        final String profile = randomString();
        final Path first = Files.createDirectories(temp.resolve("first"));
        final Path second = Files.createDirectories(temp.resolve("second"));
        final Optional<ExitCode> code = commandLine.parse(
                SERVE_COMMAND,
                "--port", String.valueOf(port),
                "--host", host,
                "--profile", profile,
                first.toString(), second.toString()
        );

        assertThat(code)
                .isEmpty();

        final ArgumentCaptor<ConfigOptions> captor = ArgumentCaptor.forClass(ConfigOptions.class);

        when(commands.serve(eq(Arrays.asList(first, second)), eq(host), eq(port), captor.capture())).thenReturn(NO_ERROR);
        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(NO_ERROR);

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(ConfigOptions::getProfile)
                .containsExactly(profile);
    }

    @Test
    void shouldValidatePortValue() {
        final Optional<ExitCode> exitCode = commandLine.parse(SERVE_COMMAND, "--port", "213123");

        assertThat(exitCode)
                .isPresent()
                .hasValue(ARGUMENT_PARSING_ERROR);
    }

    @Test
    void shouldPrintPluginList() {
        final Optional<ExitCode> exitCode = commandLine.parse(PLUGIN_COMMAND);
        assertThat(exitCode)
                .isEmpty();

        when(commands.listPlugins(any(ConfigOptions.class))).thenReturn(NO_ERROR);
        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(NO_ERROR);
    }

    @Test
    void shouldHandleVerboseOptionsWithoutArgs() {
        final String verboseOption = "-q";
        final Optional<ExitCode> exitCode = commandLine.parse(verboseOption);
        assertThat(exitCode)
                .isEmpty();
        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(ARGUMENT_PARSING_ERROR);
    }
}
