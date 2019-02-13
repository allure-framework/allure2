/*
 *  Copyright 2019 Qameta Software OÜ
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

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.qameta.allure.option.ConfigOptions;
import io.qameta.allure.option.HostPortOptions;
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
    private HostPortOptions hostPortOptions = new HostPortOptions();

    @ParametersDelegate
    private ConfigOptions configOptions = new ConfigOptions();

    public ResultsOptions getResultsOptions() {
        return resultsOptions;
    }

    public HostPortOptions getHostPortOptions() {
        return hostPortOptions;
    }

    public ConfigOptions getConfigOptions() {
        return configOptions;
    }
}
