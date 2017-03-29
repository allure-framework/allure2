package io.qameta.allure.summary;

import io.qameta.allure.Aggregator;
import io.qameta.allure.JacksonMapperContext;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.ReportConfiguration;
import io.qameta.allure.WidgetAggregator;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Statistic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class SummaryPlugin implements Aggregator, WidgetAggregator {

    @Override
    public void aggregate(final ReportConfiguration configuration,
                          final List<LaunchResults> launches,
                          final Path outputDirectory) throws IOException {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path exportFolder = Files.createDirectories(outputDirectory.resolve("export"));
        final Path summaryFile = exportFolder.resolve("summary.json");

        try (OutputStream os = Files.newOutputStream(summaryFile)) {
            context.getValue().writeValue(os, getSummaryData(launches));
        }
    }

    @Override
    public Object aggregate(ReportConfiguration configuration, List<LaunchResults> launches) {
        return getSummaryData(launches);
    }

    @Override
    public String getWidgetName() {
        return "summary";
    }

    private SummaryData getSummaryData(List<LaunchResults> launches) {
        SummaryData data = new SummaryData()
                .withStatistic(new Statistic())
                .withTime(new GroupTime())
                .withReportName("Allure Report");
        launches.stream()
                .flatMap(launch -> launch.getResults().stream())
                .forEach(result -> {
                    data.getStatistic().update(result);
                    data.getTime().update(result);
                });
        return data;
    }
}
