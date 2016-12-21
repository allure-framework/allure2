package io.qameta.allure;

import com.github.rvesse.airline.parser.errors.ParseArgumentsMissingException;
import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;
import io.qameta.allure.command.AllureCommand;
import io.qameta.allure.command.Context;
import io.qameta.allure.command.Help;
import io.qameta.allure.command.ListPlugins;
import io.qameta.allure.command.ReportGenerate;
import io.qameta.allure.command.ReportOpen;
import io.qameta.allure.command.ReportServe;
import io.qameta.allure.command.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.allurefw.allure1.AllureUtils.generateTestSuiteJsonName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static ru.yandex.qatools.matchers.nio.PathMatchers.exists;

/**
 * @author charlie (Dmitry Baev).
 */
public class CommandLineTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldParseHelpByDefault() throws Exception {
        AllureCommand cmd = new CommandLine().parse();
        assertThat(cmd, instanceOf(Help.class));
    }

    @Test
    public void shouldParseHelp() throws Exception {
        AllureCommand cmd = new CommandLine().parse("help");
        assertThat(cmd, instanceOf(Help.class));
    }

    @Test
    public void shouldParseVersion() throws Exception {
        AllureCommand cmd = new CommandLine().parse("version");
        assertThat(cmd, instanceOf(Version.class));
    }

    @Test
    public void shouldParseListPlugins() throws Exception {
        AllureCommand cmd = new CommandLine().parse("plugins");
        assertThat(cmd, instanceOf(ListPlugins.class));
    }

    @Test
    public void shouldParseReportOpen() throws Exception {
        AllureCommand cmd = new CommandLine().parse("open");
        assertThat(cmd, instanceOf(ReportOpen.class));
    }

    @Test
    public void shouldParseReportServe() throws Exception {
        String firstResult = folder.newFolder().toPath().toAbsolutePath().toString();
        String secondResult = folder.newFolder().toPath().toAbsolutePath().toString();
        AllureCommand cmd = new CommandLine().parse("serve", firstResult, secondResult);
        assertThat(cmd, instanceOf(ReportServe.class));
    }

    @Test(expected = ParseArgumentsMissingException.class)
    public void shouldFailIfNoResultsSpecifiedForServe() throws Exception {
        new CommandLine().parse("serve");
    }

    @Test(expected = ParseRestrictionViolatedException.class)
    public void shouldFailIfResultsDirectoryDoesNotExistsForServe() throws Exception {
        new CommandLine().parse("serve", "directory-does-not-exists");
    }

    @Test(expected = ParseRestrictionViolatedException.class)
    public void shouldFailIfResultsDirectoryIsFileForServe() throws Exception {
        String file = folder.newFile().toPath().toString();
        new CommandLine().parse("serve", file);
    }

    @Test
    public void shouldParseReportGenerate() throws Exception {
        String firstResult = folder.newFolder().toPath().toAbsolutePath().toString();
        String secondResult = folder.newFolder().toPath().toAbsolutePath().toString();
        AllureCommand cmd = new CommandLine().parse("generate", firstResult, secondResult);
        assertThat(cmd, instanceOf(ReportGenerate.class));
    }

    @Test
    public void shouldGenerateReport() throws Exception {
        Path results = folder.newFolder().toPath();
        copyFile(results, "testdata/sample-testsuite.json", generateTestSuiteJsonName());

        Path plugins = folder.newFolder().toPath();
        copyFile(plugins, "testdata/dummy-plugin.zip", "dummy-plugin.zip");

        Path output = folder.newFolder().toPath();

        AllureCommand generate = new CommandLine().parse(
                "generate", results.toString(), "-o", output.toString()
        );

        generate.run(new Context(
                folder.newFolder().toPath(),
                plugins,
                folder.newFolder().toPath(),
                null,
                null
        ));
        assertThat(output.resolve("index.html"), exists());
    }

    private void copyFile(Path dir, String resourceName, String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(is, dir.resolve(fileName));
        }
    }
}
