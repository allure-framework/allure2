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

import io.qameta.allure.command.MainCommand;
import io.qameta.allure.option.ConfigOptions;
import io.qameta.allure.option.ReportLanguageOptions;
import io.qameta.allure.option.ReportNameOptions;
import io.qameta.allure.option.VerboseOptions;
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

    /**
     * Verifies that invoking the parser without arguments is rejected.
     * The test checks the command line reports an argument parsing error.
     */
    @Description
    @Test
    void shouldParseEmptyArguments() {
        final Optional<ExitCode> parse = parse();
        assertThat(parse)
                .hasValue(ExitCode.ARGUMENT_PARSING_ERROR);
    }

    /**
     * Verifies parsing the verbose flag with help output.
     * The test checks verbose options are populated and the command exits successfully.
     */
    @Description
    @Test
    void shouldParseVerboseFlag() {
        final Optional<ExitCode> parse = parse("-v", "--help");

        assertThat(parse)
                .isEmpty();

        assertThat(commandLine.getMainCommand().getVerboseOptions())
                .isNotNull()
                .hasFieldOrPropertyWithValue("verbose", true)
                .hasFieldOrPropertyWithValue("quiet", false);

        final ExitCode exitCode = runCommand();
        assertThat(exitCode)
                .isEqualTo(NO_ERROR);
    }

    /**
     * Verifies parsing the quiet flag with help output.
     * The test checks quiet options are populated and the command exits successfully.
     */
    @Description
    @Test
    void shouldParseQuietFlag() {
        final Optional<ExitCode> parse = parse("-q", "--help");

        assertThat(parse)
                .isEmpty();

        assertThat(commandLine.getMainCommand().getVerboseOptions())
                .isNotNull()
                .hasFieldOrPropertyWithValue("verbose", false)
                .hasFieldOrPropertyWithValue("quiet", true);

        final ExitCode exitCode = runCommand();
        assertThat(exitCode)
                .isEqualTo(NO_ERROR);
    }

    /**
     * Verifies the generate command accepts result directories that do not exist yet.
     * The test checks parsing succeeds for arbitrary result path arguments.
     */
    @Description
    @Test
    void shouldAllowResultsDirectoriesThatNotExists() {
        final Optional<ExitCode> exitCode = parse(GENERATE_COMMAND, randomString(), randomString());
        assertThat(exitCode)
                .isEmpty();
    }

    /**
     * Verifies running the generate command delegates to the command service.
     * The test checks parsed result directories, output directory, and default flags are passed through.
     */
    @Description
    @Test
    void shouldRunGenerate(@TempDir final Path temp) throws IOException {
        final Path report = Files.createDirectories(temp.resolve("report"));
        final Path firstResult = Files.createDirectories(temp.resolve("first"));
        final Path secondResult = Files.createDirectories(temp.resolve("second"));
        final List<Path> results = Arrays.asList(firstResult, secondResult);

        when(
                commands.generate(
                        eq(report), eq(results),
                        eq(false), eq(false),
                        any(ConfigOptions.class),
                        any(ReportNameOptions.class), any(ReportLanguageOptions.class)
                )
        ).thenReturn(NO_ERROR);

        final Optional<ExitCode> exitCode = parse(
                GENERATE_COMMAND, firstResult.toString(), secondResult.toString(),
                "--output", report.toString()
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode code = runCommand();
        verify(commands, times(1))
                .generate(
                        eq(report), eq(results),
                        eq(false), eq(false),
                        any(ConfigOptions.class),
                        any(ReportNameOptions.class), any(ReportLanguageOptions.class)
                );
        assertThat(code)
                .isEqualTo(NO_ERROR);
    }

    /**
     * Verifies the generate command captures a custom report name.
     * The test checks the delegated report-name options contain the parsed name.
     */
    @Description
    @Test
    void shouldRunGenerateWithReportName(@TempDir final Path temp) throws IOException {
        final Path report = Files.createDirectories(temp.resolve("report"));
        final Path firstResult = Files.createDirectories(temp.resolve("first"));
        final Path secondResult = Files.createDirectories(temp.resolve("second"));
        final List<Path> results = Arrays.asList(firstResult, secondResult);
        final String reportName = randomString();

        when(
                commands.generate(
                        eq(report), eq(results),
                        eq(false), eq(false),
                        any(ConfigOptions.class),
                        any(ReportNameOptions.class), any(ReportLanguageOptions.class)
                )
        ).thenReturn(NO_ERROR);

        final Optional<ExitCode> exitCode = parse(
                GENERATE_COMMAND, firstResult.toString(), secondResult.toString(),
                "--output", report.toString(),
                "--name", reportName
        );
        assertThat(exitCode)
                .isEmpty();

        final ArgumentCaptor<ReportNameOptions> captor = ArgumentCaptor.captor();
        final ExitCode code = runCommand();
        verify(commands, times(1))
                .generate(
                        eq(report), eq(results), eq(false), eq(false),
                        any(ConfigOptions.class), captor.capture(), any(ReportLanguageOptions.class)
                );
        assertThat(code)
                .isEqualTo(NO_ERROR);

        assertThat(captor.getValue())
                .extracting(ReportNameOptions::getReportName)
                .isEqualTo(reportName);
    }

    /**
     * Verifies the generate command captures a custom report language.
     * The test checks the delegated language options contain the parsed language code.
     */
    @Description
    @Test
    void shouldRunGenerateWithReportLanguage(@TempDir final Path temp) throws IOException {
        final Path report = Files.createDirectories(temp.resolve("report"));
        final Path firstResult = Files.createDirectories(temp.resolve("first"));
        final Path secondResult = Files.createDirectories(temp.resolve("second"));
        final List<Path> results = Arrays.asList(firstResult, secondResult);
        final String lang = "nl";

        when(
                commands.generate(
                        eq(report), eq(results),
                        eq(false), eq(false),
                        any(ConfigOptions.class),
                        any(ReportNameOptions.class), any(ReportLanguageOptions.class)
                )
        ).thenReturn(NO_ERROR);

        final Optional<ExitCode> exitCode = parse(
                GENERATE_COMMAND, firstResult.toString(), secondResult.toString(),
                "--output", report.toString(),
                "--lang", lang
        );
        assertThat(exitCode)
                .isEmpty();

        final ArgumentCaptor<ReportLanguageOptions> captor = ArgumentCaptor.captor();
        final ExitCode code = runCommand();
        verify(commands, times(1))
                .generate(
                        eq(report), eq(results), eq(false), eq(false),
                        any(ConfigOptions.class), any(ReportNameOptions.class), captor.capture()
                );
        assertThat(code)
                .isEqualTo(NO_ERROR);

        assertThat(captor.getValue())
                .extracting(ReportLanguageOptions::getReportLanguage)
                .isEqualTo(lang);
    }

    /**
     * Verifies running the open command delegates to the command service.
     * The test checks the parsed report directory and port are passed through.
     */
    @Description
    @Test
    void shouldRunOpen(@TempDir final Path report) {
        final int port = randomPort();
        when(commands.open(report, null, port))
                .thenReturn(NO_ERROR);

        final Optional<ExitCode> exitCode = parse(
                OPEN_COMMAND, "--port", String.valueOf(port), report.toString()
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode code = runCommand();
        verify(commands, times(1)).open(report, null, port);
        assertThat(code)
                .isEqualTo(NO_ERROR);
    }

    /**
     * Verifies the open command rejects multiple report directories.
     * The test checks parsing returns an argument parsing error for ambiguous input.
     */
    @Description
    @Test
    void shouldNotLetToSpecifyFewReportDirectories(@TempDir final Path temp) throws IOException {
        final Path first = Files.createDirectories(temp.resolve("first"));
        final Path second = Files.createDirectories(temp.resolve("second"));

        final Optional<ExitCode> exitCode = parse(
                OPEN_COMMAND, first.toString(), second.toString()
        );
        assertThat(exitCode)
                .isPresent()
                .hasValue(ExitCode.ARGUMENT_PARSING_ERROR);
    }

    /**
     * Verifies command-specific help can be parsed and run.
     * The test checks help for the open command exits successfully.
     */
    @Description
    @Test
    void shouldRunHelpForCommand() {
        final Optional<ExitCode> exitCode = parse(
                "--help", OPEN_COMMAND
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode run = runCommand();
        assertThat(run)
                .isEqualTo(NO_ERROR);
    }

    /**
     * Verifies the version flag can be parsed and run.
     * The test checks version output exits successfully without a subcommand.
     */
    @Description
    @Test
    void shouldPrintVersion() {
        final Optional<ExitCode> exitCode = parse(
                "--version"
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode run = runCommand();
        assertThat(run)
                .isEqualTo(NO_ERROR);
    }

    /**
     * Verifies the serve command parses host, port, profile, and result directories.
     * The test checks the delegated config options include the requested profile.
     */
    @Description
    @Test
    void shouldParseServeCommand(@TempDir final Path temp) throws IOException {
        final int port = randomPort();
        final String host = randomString();
        final String profile = randomString();
        final Path first = Files.createDirectories(temp.resolve("first"));
        final Path second = Files.createDirectories(temp.resolve("second"));
        final Optional<ExitCode> code = parse(
                SERVE_COMMAND,
                "--port", String.valueOf(port),
                "--host", host,
                "--profile", profile,
                first.toString(), second.toString()
        );

        assertThat(code)
                .isEmpty();

        final ArgumentCaptor<ConfigOptions> captor = ArgumentCaptor.captor();

        when(
                commands.serve(
                        eq(Arrays.asList(first, second)), eq(host), eq(port),
                        captor.capture(), any(ReportNameOptions.class), any(ReportLanguageOptions.class)
                )
        )
                .thenReturn(NO_ERROR);
        final ExitCode run = runCommand();
        assertThat(run)
                .isEqualTo(NO_ERROR);

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(ConfigOptions::getProfile)
                .containsExactly(profile);
    }

    /**
     * Verifies the serve command parses a custom report name.
     * The test checks the delegated report-name options contain the requested name.
     */
    @Description
    @Test
    void shouldParseServeCommandWithReportName(@TempDir final Path temp) throws IOException {
        final int port = randomPort();
        final String host = randomString();
        final String profile = randomString();
        final String reportName = randomString();
        final Path first = Files.createDirectories(temp.resolve("first"));
        final Path second = Files.createDirectories(temp.resolve("second"));
        final Optional<ExitCode> code = parse(
                SERVE_COMMAND,
                "--port", String.valueOf(port),
                "--host", host,
                "--profile", profile,
                "--report-name", reportName,
                first.toString(), second.toString()
        );

        assertThat(code)
                .isEmpty();

        final ArgumentCaptor<ConfigOptions> captorConfig = ArgumentCaptor.captor();
        final ArgumentCaptor<ReportNameOptions> captorReportName = ArgumentCaptor.captor();

        when(
                commands.serve(
                        eq(Arrays.asList(first, second)), eq(host), eq(port),
                        captorConfig.capture(), captorReportName.capture(), any(ReportLanguageOptions.class)
                )
        )
                .thenReturn(NO_ERROR);
        final ExitCode run = runCommand();
        assertThat(run)
                .isEqualTo(NO_ERROR);

        assertThat(captorConfig.getAllValues())
                .hasSize(1)
                .extracting(ConfigOptions::getProfile)
                .containsExactly(profile);

        assertThat(captorReportName.getValue())
                .extracting(ReportNameOptions::getReportName)
                .isEqualTo(reportName);
    }

    /**
     * Verifies the serve command parses a custom report language.
     * The test checks the delegated language options contain the requested language code.
     */
    @Description
    @Test
    void shouldParseServeCommandWithReportLanguage(@TempDir final Path temp) throws IOException {
        final int port = randomPort();
        final String host = randomString();
        final String profile = randomString();
        final String lang = "de";
        final Path first = Files.createDirectories(temp.resolve("first"));
        final Path second = Files.createDirectories(temp.resolve("second"));
        final Optional<ExitCode> code = parse(
                SERVE_COMMAND,
                "--port", String.valueOf(port),
                "--host", host,
                "--profile", profile,
                "--lang", lang,
                first.toString(), second.toString()
        );

        assertThat(code)
                .isEmpty();

        final ArgumentCaptor<ConfigOptions> captorConfig = ArgumentCaptor.captor();
        final ArgumentCaptor<ReportLanguageOptions> captorReportLang = ArgumentCaptor.captor();

        when(
                commands.serve(
                        eq(Arrays.asList(first, second)), eq(host), eq(port),
                        captorConfig.capture(), any(ReportNameOptions.class), captorReportLang.capture()
                )
        )
                .thenReturn(NO_ERROR);
        final ExitCode run = runCommand();
        assertThat(run)
                .isEqualTo(NO_ERROR);

        assertThat(captorConfig.getAllValues())
                .hasSize(1)
                .extracting(ConfigOptions::getProfile)
                .containsExactly(profile);

        assertThat(captorReportLang.getValue())
                .extracting(ReportLanguageOptions::getReportLanguage)
                .isEqualTo(lang);
    }

    /**
     * Verifies serve command language validation.
     * The test checks an unsupported language value is rejected during parsing.
     */
    @Description
    @Test
    void shouldValidateLanguageValue() {
        final Optional<ExitCode> exitCode = parse(SERVE_COMMAND, "--lang", "invalid");

        assertThat(exitCode)
                .isPresent()
                .hasValue(ARGUMENT_PARSING_ERROR);
    }

    /**
     * Verifies serve command port validation.
     * The test checks an out-of-range port value is rejected during parsing.
     */
    @Description
    @Test
    void shouldValidatePortValue() {
        final Optional<ExitCode> exitCode = parse(SERVE_COMMAND, "--port", "213123");

        assertThat(exitCode)
                .isPresent()
                .hasValue(ARGUMENT_PARSING_ERROR);
    }

    /**
     * Verifies the plugin command lists configured plugins.
     * The test checks plugin listing is delegated and exits successfully.
     */
    @Description
    @Test
    void shouldPrintPluginList() {
        final Optional<ExitCode> exitCode = parse(PLUGIN_COMMAND);
        assertThat(exitCode)
                .isEmpty();

        when(commands.listPlugins(any(ConfigOptions.class))).thenReturn(NO_ERROR);
        final ExitCode run = runCommand();
        assertThat(run)
                .isEqualTo(NO_ERROR);
    }

    /**
     * Verifies verbose options can be parsed without a command while execution still fails.
     * The test checks parsing accepts the option and running returns an argument parsing error.
     */
    @Description
    @Test
    void shouldHandleVerboseOptionsWithoutArgs() {
        final String verboseOption = "-q";
        final Optional<ExitCode> exitCode = parse(verboseOption);
        assertThat(exitCode)
                .isEmpty();
        final ExitCode run = runCommand();
        assertThat(run)
                .isEqualTo(ARGUMENT_PARSING_ERROR);
    }

    @Step("Parse command line arguments")
    private Optional<ExitCode> parse(final String... arguments) {
        final Optional<ExitCode> result = commandLine.parse(arguments);
        attachCommandParse(arguments, result);
        return result;
    }

    @Step("Run parsed command")
    private ExitCode runCommand() {
        final ExitCode result = commandLine.run();
        Allure.addAttachment(
                "command-run.txt",
                "text/plain",
                String.format("runResult=%s%n%s", result, describeMainCommand()),
                ".txt"
        );
        return result;
    }

    private void attachCommandParse(final String[] arguments, final Optional<ExitCode> result) {
        Allure.addAttachment(
                "command-parse.txt",
                "text/plain",
                String.format(
                        "arguments=%s%nparseResult=%s%n%s",
                        Arrays.toString(arguments),
                        result.map(ExitCode::name).orElse("<empty>"),
                        describeMainCommand()
                ),
                ".txt"
        );
    }

    private String describeMainCommand() {
        final MainCommand mainCommand = commandLine.getMainCommand();
        final VerboseOptions verboseOptions = mainCommand.getVerboseOptions();
        return String.format(
                "help=%s%nversion=%s%nverbose=%s%nquiet=%s%n",
                mainCommand.isHelp(),
                mainCommand.isVersion(),
                verboseOptions.isVerbose(),
                verboseOptions.isQuiet()
        );
    }
}
