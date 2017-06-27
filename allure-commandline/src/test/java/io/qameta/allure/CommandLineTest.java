package io.qameta.allure;

import io.qameta.allure.option.ConfigOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

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
public class CommandLineTest {

    private Commands commands;
    private CommandLine commandLine;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        this.commands = mock(Commands.class);
        this.commandLine = new CommandLine(commands);
    }

    @Test
    public void shouldParseEmptyArguments() throws Exception {
        final Optional<ExitCode> parse = commandLine.parse();
        assertThat(parse)
                .hasValue(ExitCode.ARGUMENT_PARSING_ERROR);
    }

    @Test
    public void shouldParseVerboseFlag() throws Exception {
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
    public void shouldParseQuietFlag() throws Exception {
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
    public void shouldAllowResultsDirectoriesThatNotExists() throws Exception {
        final Optional<ExitCode> exitCode = commandLine.parse(GENERATE_COMMAND, randomString(), randomString());
        assertThat(exitCode)
                .isEmpty();
    }

    @Test
    public void shouldRunGenerate() throws Exception {
        final Path report = folder.newFolder().toPath();
        final Path firstResult = folder.newFolder().toPath();
        final Path secondResult = folder.newFolder().toPath();
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
    public void shouldRunOpen() throws Exception {
        final int port = randomPort();
        final Path report = folder.newFolder().toPath();
        when(commands.open(report, port))
                .thenReturn(NO_ERROR);

        final Optional<ExitCode> exitCode = commandLine.parse(
                OPEN_COMMAND, "--port", String.valueOf(port), report.toString()
        );
        assertThat(exitCode)
                .isEmpty();

        final ExitCode code = commandLine.run();
        verify(commands, times(1)).open(report, port);
        assertThat(code)
                .isEqualTo(NO_ERROR);
    }

    @Test
    public void shouldNotLetToSpecifyFewReportDirectories() throws Exception {
        final Path first = folder.newFolder().toPath();
        final Path second = folder.newFolder().toPath();

        final Optional<ExitCode> exitCode = commandLine.parse(
                OPEN_COMMAND, first.toString(), second.toString()
        );
        assertThat(exitCode)
                .isPresent()
                .hasValue(ExitCode.ARGUMENT_PARSING_ERROR);
    }

    @Test
    public void shouldRunHelpForCommand() throws Exception {
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
    public void shouldPrintVersion() throws Exception {
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
    public void shouldParseServeCommand() throws Exception {
        final int port = randomPort();
        final String profile = randomString();
        final Path first = folder.newFolder().toPath();
        final Path second = folder.newFolder().toPath();
        final Optional<ExitCode> code = commandLine.parse(
                SERVE_COMMAND,
                "--port", String.valueOf(port),
                "--profile", profile,
                first.toString(), second.toString()
        );

        assertThat(code)
                .isEmpty();

        final ArgumentCaptor<ConfigOptions> captor = ArgumentCaptor.forClass(ConfigOptions.class);

        when(commands.serve(eq(Arrays.asList(first, second)), eq(port), captor.capture())).thenReturn(NO_ERROR);
        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(NO_ERROR);

        assertThat(captor.getAllValues())
                .hasSize(1)
                .extracting(ConfigOptions::getProfile)
                .containsExactly(profile);
    }

    @Test
    public void shouldValidatePortValue() throws Exception {
        final Optional<ExitCode> exitCode = commandLine.parse(SERVE_COMMAND, "--port", "213123");

        assertThat(exitCode)
                .isPresent()
                .hasValue(ARGUMENT_PARSING_ERROR);
    }

    @Test
    public void shouldPrintPluginList() throws Exception {
        final Optional<ExitCode> exitCode = commandLine.parse(PLUGIN_COMMAND);
        assertThat(exitCode)
                .isEmpty();

        when(commands.listPlugins(any(ConfigOptions.class))).thenReturn(NO_ERROR);
        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(NO_ERROR);
    }

    @Test
    public void shouldHandleVerboseOptionsWithoutArgs() {
        final String verboseOption = "-q";
        final Optional<ExitCode> exitCode = commandLine.parse(verboseOption);
        assertThat(exitCode)
                .isEmpty();
        final ExitCode run = commandLine.run();
        assertThat(run)
                .isEqualTo(ARGUMENT_PARSING_ERROR);
    }
}