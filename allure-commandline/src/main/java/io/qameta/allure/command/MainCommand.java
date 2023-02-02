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
