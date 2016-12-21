package io.qameta.allure.timeline;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeResultAggregator;
import io.qameta.allure.tree.TreeGroup;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 17.04.16
 */
public class TimelineResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return Arrays.asList(
                TreeGroup.oneByLabel(result, LabelName.HOST, "Default hostname"),
                TreeGroup.oneByLabel(result, LabelName.THREAD, "Default thread")
        );
    }
}
