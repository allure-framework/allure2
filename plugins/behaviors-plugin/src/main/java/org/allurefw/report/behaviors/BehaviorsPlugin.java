package org.allurefw.report.behaviors;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 31.01.16
 */
@Plugin(name = "behaviors")
public class BehaviorsPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(BehaviorsAggregator.class)
                .toReportData("behaviors.json");
    }
}
