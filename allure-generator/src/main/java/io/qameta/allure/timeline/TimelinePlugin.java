package io.qameta.allure.timeline;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.AbstractTreeAggregator;
import io.qameta.allure.tree.TreeGroup;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Plugin that generates data for Timeline tab.
 *
 * @since 2.0
 */
public class TimelinePlugin extends AbstractTreeAggregator {

    @Override
    protected String getFileName() {
        return "timeline.json";
    }

    @Override
    protected List<TreeGroup> getGroups(final TestResult result) {
        return Arrays.asList(
                TreeGroup.oneByLabel(result, LabelName.HOST, "Default hostname"),
                TreeGroup.oneByLabel(result, LabelName.THREAD, "Default thread")
        );
    }

    @Override
    protected Stream<TestResult> getTestResults(final LaunchResults launchResults) {
        return launchResults.getAllResults().stream();
    }
}
