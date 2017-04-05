package io.qameta.allure.widget;

import io.qameta.allure.Aggregator;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plugin generates widgets.json for Overview tab.
 *
 * @since 2.0
 */
public class WidgetsPlugin implements Aggregator {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path widgetsFile = dataFolder.resolve("widgets.json");

        try (OutputStream os = Files.newOutputStream(widgetsFile)) {
            context.getValue().writeValue(os, getData(configuration, launchesResults));
        }
    }

    protected Object getData(final Configuration configuration,
                             final List<LaunchResults> launches) {
        final Map<String, Object> data = new HashMap<>();
        configuration.getWidgets().forEach(widgetAggregator -> data.put(
                widgetAggregator.getName(),
                widgetAggregator.getData(configuration, launches)
        ));
        return data;
    }
}
