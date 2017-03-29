package io.qameta.allure;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WidgetAggregator {

    Object aggregate(ReportConfiguration configuration,
                     List<LaunchResults> launches);

    String getWidgetName();
}
