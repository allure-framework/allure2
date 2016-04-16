package org.allurefw.report.defects;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.PluginScope;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 01.02.16
 */
@Plugin(name = "defects", scope = PluginScope.PROCESS)
public class DefectsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        use(DefectsAggregator.class)
                .asAggregator()
                .toReportData("defects.json");

    }
}
