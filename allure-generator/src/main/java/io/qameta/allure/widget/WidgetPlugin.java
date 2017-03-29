package io.qameta.allure.widget;

import io.qameta.allure.Aggregator;
import io.qameta.allure.JacksonMapperContext;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.ReportConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author charlie (Dmitry Baev).
 */
public class WidgetPlugin implements Aggregator {

    @Override
    public void aggregate(final ReportConfiguration configuration,
                          final List<LaunchResults> launches,
                          final Path outputDirectory) throws IOException {
        final JacksonMapperContext context = configuration.requireContext(JacksonMapperContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path widgetsFile = dataFolder.resolve("widgets.json");

        try (OutputStream os = Files.newOutputStream(widgetsFile)) {
            context.getValue().writeValue(os, getData(configuration, launches));
        }
    }

    protected Object getData(final ReportConfiguration configuration,
                             final List<LaunchResults> launches) {
        final Map<String, Object> data = new HashMap<>();
        configuration.getWidgetAggregators().forEach(widgetAggregator -> data.put(
                widgetAggregator.getWidgetName(),
                widgetAggregator.aggregate(configuration, launches)
        ));
        return data;
    }
}
