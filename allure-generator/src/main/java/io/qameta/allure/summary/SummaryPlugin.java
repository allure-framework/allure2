package io.qameta.allure.summary;

import io.qameta.allure.Configuration;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.Plugin;
import io.qameta.allure.WidgetPlugin;
import io.qameta.allure.context.JacksonContext;
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
public class SummaryPlugin implements Plugin, WidgetPlugin {

    @Override
    public void process(final Configuration configuration,
                        final List<LaunchResults> launches,
                        final Path outputDirectory) throws IOException {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path exportFolder = Files.createDirectories(outputDirectory.resolve("export"));
        final Path summaryFile = exportFolder.resolve("summary.json");

        try (OutputStream os = Files.newOutputStream(summaryFile)) {
            context.getValue().writeValue(os, getSummaryData(launches));
        }
    }

    @Override
    public Object getWidgetData(final Configuration configuration, final List<LaunchResults> launches) {
        return getSummaryData(launches);
    }

    @Override
    public String getWidgetName() {
        return "summary";
    }

    private SummaryData getSummaryData(final List<LaunchResults> launches) {
        final SummaryData data = new SummaryData()
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
