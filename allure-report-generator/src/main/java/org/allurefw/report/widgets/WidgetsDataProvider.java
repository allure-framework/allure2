package org.allurefw.report.widgets;

import com.google.inject.Inject;
import org.allurefw.report.ReportDataProvider;
import org.allurefw.report.WidgetDataProvider;

import java.util.HashMap;
import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class WidgetsDataProvider implements ReportDataProvider {

    protected final Set<WidgetDataProvider> widgets;

    @Inject
    public WidgetsDataProvider(Set<WidgetDataProvider> widgets) {
        this.widgets = widgets;
    }

    @Override
    public Object provide() {
        return widgets.stream().collect(
                HashMap::new,
                (map, provider) -> map.put(provider.getWidgetId(), provider.provide()),
                HashMap::putAll
        );
    }

    @Override
    public String getFileName() {
        return "widgets.json";
    }
}
