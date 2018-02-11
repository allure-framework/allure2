package io.qameta.allure;

import io.qameta.allure.entity.TestResult;
import io.qameta.allure.metric.Metric;
import io.qameta.allure.metric.MetricLine;
import io.qameta.allure.service.TestResultService;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public abstract class AbstractMetricAggregator implements Aggregator {

    private final String location;

    private final String fileName;

    protected AbstractMetricAggregator(final String fileName) {
        this("export", fileName);
    }

    protected AbstractMetricAggregator(final String location, final String fileName) {
        this.location = location;
        this.fileName = fileName;
    }

    @Override
    public void aggregate(final ReportContext context, final TestResultService testResultService,
                          final Path outputDirectory) throws IOException {
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve(location));
        final Path dataFile = dataFolder.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
            writer.write(getData(testResultService));
        }
    }

    public abstract List<Metric> getMetrics();

    protected String getData(final TestResultService testResultService) {
        final List<Metric> metrics = getMetrics();
        for (TestResult result : testResultService.findAllTests()) {
            for (Metric metric : metrics) {
                metric.update(result);
            }
        }

        return metrics.stream()
                .map(Metric::getLines)
                .flatMap(Collection::stream)
                .map(MetricLine::asString)
                .collect(Collectors.joining("\n", "", "\n"));
    }

}
