package io.qameta.allure.command;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.qameta.allure.option.ProfileOptions;

/**
 * Display plugins.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
@Parameters(commandDescription = "Generate the report")
public class PluginCommand {

    @ParametersDelegate
    private ProfileOptions profileOptions = new ProfileOptions();

    public ProfileOptions getProfileOptions() {
        return profileOptions;
    }
}
