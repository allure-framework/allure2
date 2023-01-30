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
 * Contains options to configure commandline verbosity.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class VerboseOptions {

    @Parameter(
            names = {"-v", "--verbose"},
            description = "Switch on the verbose mode."
    )
    private boolean verbose;

    @Parameter(
            names = {"-q", "--quiet"},
            description = "Switch on the quiet mode."
    )
    private boolean quiet;

    /**
     * Returns true if silent mode is enabled, false otherwise.
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Returns true if verbose mode is enabled, false otherwise.
     */
    public boolean isVerbose() {
        return verbose;
    }
}
