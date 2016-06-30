package org.allurefw.report.defects;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
@Plugin(name = "defects")
public class DefectsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(DefectsAggregator.class)
                .toReportData("defects.json")
                .toWidget("productDefects", ProductDefectsWidgetFinalizer.class)
                .toWidget("testDefects", TestDefectsWidgetFinalizer.class);

    }
}
