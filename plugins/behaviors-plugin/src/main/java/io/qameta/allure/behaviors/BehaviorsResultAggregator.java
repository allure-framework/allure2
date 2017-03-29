package io.qameta.allure.behaviors;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeAggregator;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class BehaviorsResultAggregator extends TreeAggregator {

    @Override
    protected List<TreeGroup> getGroups(final TestCaseResult result) {
        return Arrays.asList(
                TreeGroup.allByLabel(result, LabelName.FEATURE, "Default feature"),
                TreeGroup.allByLabel(result, LabelName.STORY, "Default story")
        );
    }
}
