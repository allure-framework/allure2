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
