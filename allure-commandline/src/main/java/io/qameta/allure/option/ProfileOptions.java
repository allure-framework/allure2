package io.qameta.allure.option;

import com.beust.jcommander.Parameter;

/**
 * Contains profile options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class ProfileOptions {

    @Parameter(
            names = {"--profile"},
            description = "Allure commandline configuration profile."
    )
    private String profile;

    public String getProfile() {
        return profile;
    }
}
