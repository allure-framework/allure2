package io.qameta.allure.command;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.qameta.allure.option.PortOptions;
import io.qameta.allure.option.ProfileOptions;
import io.qameta.allure.option.ResultsOptions;

/**
 * Contains options for serve command.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
@Parameters(commandDescription = "Serve the report")
public class ServeCommand {

    @ParametersDelegate
    private ResultsOptions resultsOptions = new ResultsOptions();

    @ParametersDelegate
    private PortOptions portOptions = new PortOptions();

    @ParametersDelegate
    private ProfileOptions profileOptions = new ProfileOptions();

    public ResultsOptions getResultsOptions() {
        return resultsOptions;
    }

    public PortOptions getPortOptions() {
        return portOptions;
    }

    public ProfileOptions getProfileOptions() {
        return profileOptions;
    }
}
