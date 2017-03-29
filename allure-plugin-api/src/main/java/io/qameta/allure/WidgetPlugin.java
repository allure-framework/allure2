package io.qameta.allure;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WidgetPlugin {

    Object getWidgetData(Configuration configuration,
                         List<LaunchResults> launches);

    String getWidgetName();
}
