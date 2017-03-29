package io.qameta.allure.widget;

import io.qameta.allure.Configuration;
import io.qameta.allure.LaunchResults;
import io.qameta.allure.Plugin;
import io.qameta.allure.context.JacksonContext;

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
public class WidgetsPlugin implements Plugin {

    @Override
    public void process(final Configuration configuration,
                        final List<LaunchResults> launches,
                        final Path outputDirectory) throws IOException {
        final JacksonContext context = configuration.requireContext(JacksonContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path widgetsFile = dataFolder.resolve("widgets.json");

        try (OutputStream os = Files.newOutputStream(widgetsFile)) {
            context.getValue().writeValue(os, getData(configuration, launches));
        }
    }

    protected Object getData(final Configuration configuration,
                             final List<LaunchResults> launches) {
        final Map<String, Object> data = new HashMap<>();
        configuration.getWidgetPlugins().forEach(widgetAggregator -> data.put(
                widgetAggregator.getWidgetName(),
                widgetAggregator.getWidgetData(configuration, launches)
        ));
        return data;
    }
}
