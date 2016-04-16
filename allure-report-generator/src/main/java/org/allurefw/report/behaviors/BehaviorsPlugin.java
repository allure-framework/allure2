package org.allurefw.report.behaviors;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginScope;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
@Plugin(name = "behaviors", scope = PluginScope.PROCESS)
public class BehaviorsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(BehaviorsAggregator.class)
                .toReportData("behaviors.json");
    }
}
