package org.allurefw.report.packages;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.Plugin;
import org.allurefw.report.tree.TreeCollapseGroupsWithOneChildFinalizer;

/**
 * @author charlie (Dmitry Baev).
 */
@Plugin(name = "packages")
public class PackagesPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregator(PackagesAggregator.class)
                .toReportData("packages.json", TreeCollapseGroupsWithOneChildFinalizer.class);
    }
}
