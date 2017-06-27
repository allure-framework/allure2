package io.qameta.allure.option;

import com.beust.jcommander.Parameter;
import io.qameta.allure.convert.PathConverter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Contains results options.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ImmutableField")
public class ResultsOptions {

    @Parameter(
            description = "The directories with allure results",
            converter = PathConverter.class
    )
    private List<Path> resultsDirectories = new ArrayList<>(singletonList(Paths.get("allure-results")));

    public List<Path> getResultsDirectories() {
        return resultsDirectories;
    }
}
