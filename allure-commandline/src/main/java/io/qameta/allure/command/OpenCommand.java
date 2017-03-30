package io.qameta.allure.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.qameta.allure.convert.PathConverter;
import io.qameta.allure.option.PortOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains options for open command.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
@Parameters(commandDescription = "Open generated report")
public class OpenCommand {

    @Parameter(
            description = "The report directory",
            converter = PathConverter.class
    )
    private List<Path> reportDirectories = new ArrayList<>(Collections.singletonList(Paths.get("allure-report")));

    @ParametersDelegate
    private PortOptions portOptions = new PortOptions();

    public List<Path> getReportDirectories() {
        return reportDirectories;
    }

    public PortOptions getPortOptions() {
        return portOptions;
    }
}