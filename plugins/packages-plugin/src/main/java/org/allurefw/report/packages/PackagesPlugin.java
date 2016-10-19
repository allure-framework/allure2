package org.allurefw.report.packages;

import org.allurefw.report.AbstractPlugin;
import org.allurefw.report.tree.TreeCollapseGroupsWithOneChildFinalizer;

/**
 * @author charlie (Dmitry Baev).
 */
public class PackagesPlugin extends AbstractPlugin {

    @Override
    protected void configure() {
        aggregateResults(PackagesResultAggregator.class)
                .toReportData("packages.json", TreeCollapseGroupsWithOneChildFinalizer.class);
    }
}
