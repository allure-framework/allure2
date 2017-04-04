package io.qameta.allure.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.qameta.allure.option.VerboseOptions;

/**
 * Contains main commandline options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
@Parameters(commandNames = "allure", commandDescription = "Allure Commandline")
public class MainCommand {

    @Parameter(
            names = "--help",
            description = "Print commandline help.",
            help = true
    )
    private boolean help;

    @Parameter(
            names = "--version",
            description = "Print commandline version."
    )
    private boolean version;

    @ParametersDelegate
    private VerboseOptions verboseOptions = new VerboseOptions();

    public boolean isHelp() {
        return help;
    }

    public boolean isVersion() {
        return version;
    }

    public VerboseOptions getVerboseOptions() {
        return verboseOptions;
    }
}
