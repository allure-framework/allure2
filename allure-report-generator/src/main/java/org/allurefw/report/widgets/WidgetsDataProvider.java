package org.allurefw.report.widgets;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.allurefw.report.WidgetData;

import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
public class WidgetsDataProvider implements Provider<Object> {

    protected final Map<String, Object> widgetData;

    @Inject
    public WidgetsDataProvider(@WidgetData Map<String, Object> widgetData) {
        this.widgetData = widgetData;
    }

    @Override
    public Object get() {
        return widgetData;
    }
}
