package io.qameta.allure.behaviors;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeResultAggregator;
import io.qameta.allure.tree.TreeGroup;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 16.04.16
 */
public class BehaviorsResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return Arrays.asList(
                TreeGroup.allByLabel(result, LabelName.FEATURE, "Default feature"),
                TreeGroup.allByLabel(result, LabelName.STORY, "Default story")
        );
    }
}
