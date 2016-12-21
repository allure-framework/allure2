package io.qameta.allure.command;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class ResultsOptions {

    @Path(mustExist = true, kind = PathKind.DIRECTORY)
    @Required
    @Arguments(
            title = "Results directories",
            description = "A list of results directories to be processed")
    protected List<String> resultsDirectories = new ArrayList<>();

    public java.nio.file.Path[] getResultsDirectories() {
        return resultsDirectories.stream().map(Paths::get).toArray(java.nio.file.Path[]::new);
    }
}
