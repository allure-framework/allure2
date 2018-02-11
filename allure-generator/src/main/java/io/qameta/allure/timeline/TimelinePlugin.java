package io.qameta.allure.timeline;

import io.qameta.allure.AbstractJsonAggregator;
import io.qameta.allure.ReportContext;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.service.TestResultService;
import io.qameta.allure.tree.TestResultTree;

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
    protected TestResultTree getData(final ReportContext context, final TestResultService testResultService) {

        // @formatter:off
        final TestResultTree timeline = new TestResultTree(
            "timeline",
            testResult -> groupByLabels(testResult, LabelName.HOST, LabelName.THREAD)
        );
        // @formatter:on

        testResultService
                .findAll()
                .forEach(timeline::add);
        return timeline;
    }
}
