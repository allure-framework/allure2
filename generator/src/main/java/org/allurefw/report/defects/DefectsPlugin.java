package org.allurefw.report.defects;

import org.allurefw.report.AbstractPlugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 01.02.16
 */
public class DefectsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(DefectsResultAggregator.class)
                .toReportData("defects.json")
                .toWidget("productDefects", ProductDefectsWidgetFinalizer.class)
                .toWidget("testDefects", TestDefectsWidgetFinalizer.class);

    }
}
