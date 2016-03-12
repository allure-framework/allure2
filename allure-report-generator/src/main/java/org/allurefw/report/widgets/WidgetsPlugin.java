package org.allurefw.report.widgets;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 17.02.16
 */
@Plugin(name = "widget-support")
public class WidgetsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        reportDataBuilder("widgets.json").toProvider(WidgetsDataProvider.class);
    }
}
