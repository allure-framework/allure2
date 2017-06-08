package io.qameta.allure.option;

import com.beust.jcommander.Parameter;

/**
 * Contains profile options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class ConfigOptions {

    @Parameter(
            names = {"--configDirectory"},
            description = "Allure commandline configurations directory. "
                    + "By default uses ALLURE_HOME directory."
    )
    private String configDirectory;

    @Parameter(
            names = {"--profile"},
            description = "Allure commandline configuration profile."
    )
    private String profile;

    @Parameter(
            names = {"--config"},
            description = "Allure commandline config path. If specified "
                    + "overrides values from --profile and --configDirectory."
    )
    private String configPath;

    public String getProfile() {
        return profile;
    }

    public String getConfigDirectory() {
        return configDirectory;
    }

    public String getConfigPath() {
        return configPath;
    }
}
