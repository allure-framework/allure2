package io.qameta.allure;

import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Common json aggregator.
 */
public abstract class CommonJsonAggregator implements Aggregator {

    private final String location;

    private final String fileName;

    protected CommonJsonAggregator(final String fileName) {
        this(Constants.DATA_DIR, fileName);
    }

    protected CommonJsonAggregator(final String location, final String fileName) {
        this.location = location;
        this.fileName = fileName;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve(this.location));
        final Path dataFile = dataFolder.resolve(this.fileName);
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, getData(launchesResults));
        }
    }

    protected abstract Object getData(List<LaunchResults> launches);
}
