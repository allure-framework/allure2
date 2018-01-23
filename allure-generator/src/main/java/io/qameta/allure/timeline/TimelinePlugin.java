package io.qameta.allure.timeline;

import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.tree.TestResultTree;

import java.util.Collection;
import java.util.List;

import static io.qameta.allure.tree.TreeUtils.groupByLabels;

/**
 * Plugin that generates data for Timeline tab.
 *
 * @since 2.0
 */
public class TimelinePlugin extends AbstractJsonAggregator {

    public TimelinePlugin() {
        super("timeline.json");
    }

    @Override
    protected TestResultTree getData(final List<LaunchResults> launchResults) {

        // @formatter:off
        final TestResultTree timeline = new TestResultTree(
            "timeline",
            testResult -> groupByLabels(testResult, LabelName.HOST, LabelName.THREAD)
        );
        // @formatter:on

        launchResults.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .forEach(timeline::add);
        return timeline;
    }
}
