package io.qameta.allure.influxdb;

import io.qameta.allure.Aggregator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Plugin that generates data for influx db.
 */
public class InfluxDbExportPlugin implements Aggregator {

    private static final String FILE_NAME = "influxDbData.txt";

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("export"));
        final Path dataFile = dataFolder.resolve(FILE_NAME);
        try (Writer writer = Files.newBufferedWriter(dataFile, Charset.forName("UTF-8"))) {
            InfluxDbExportItem item = getData(launchesResults);
            writer.write(item.toString());
        }
    }

    private InfluxDbExportItem getData(final List<LaunchResults> launchesResults) {
        InfluxDbExportItem item = new InfluxDbExportItem();
        launchesResults.stream()
                .flatMap(launch -> launch.getAllResults().stream())
                .forEach(item::updateMetrics);
        return item;
    }
}
