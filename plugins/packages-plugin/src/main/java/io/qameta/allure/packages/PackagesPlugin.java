package io.qameta.allure.packages;

import io.qameta.allure.AbstractPlugin;
import io.qameta.allure.tree.TreeCollapseGroupsWithOneChildFinalizer;

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
