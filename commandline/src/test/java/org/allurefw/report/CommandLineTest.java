package org.allurefw.report;

import com.github.rvesse.airline.parser.errors.ParseArgumentsMissingException;
import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;
import org.allurefw.report.command.AllureCommand;
import org.allurefw.report.command.Help;
import org.allurefw.report.command.ListPlugins;
import org.allurefw.report.command.ReportGenerate;
import org.allurefw.report.command.ReportOpen;
import org.allurefw.report.command.ReportServe;
import org.allurefw.report.command.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

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
}
