package org.allurefw.report.environment;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 17.02.16
 */
@Plugin(name = "environment")
public class EnvironmentPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(EnvironmentAggregator.class)
                .toReportData("environment.json")
                .toWidget("environment", EnvironmentFinalizer.class);
    }
}
