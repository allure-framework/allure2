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
import io.qameta.allure.convert.PathConverter;
import io.qameta.allure.option.ConfigOptions;
import io.qameta.allure.option.ResultsOptions;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains options for generate command.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
@Parameters(commandDescription = "Generate the report")
public class GenerateCommand {

    @Parameter(
            names = {"-c", "--clean"},
            description = "Clean Allure report directory before generating a new one."
    )
    private boolean cleanReportDirectory;

    @Parameter(
            names = {"-o", "--report-dir", "--output"},
            description = "The directory to generate Allure report into.",
            converter = PathConverter.class
    )
    private Path reportDirectory = Paths.get("allure-report");

    @ParametersDelegate
    private ResultsOptions resultsOptions = new ResultsOptions();

    @ParametersDelegate
    private ConfigOptions configOptions = new ConfigOptions();

    public boolean isCleanReportDirectory() {
        return cleanReportDirectory;
    }

    public Path getReportDirectory() {
        return reportDirectory;
    }

    public ResultsOptions getResultsOptions() {
        return resultsOptions;
    }

    public ConfigOptions getConfigOptions() {
        return configOptions;
    }
}
