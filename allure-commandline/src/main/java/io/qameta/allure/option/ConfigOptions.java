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
