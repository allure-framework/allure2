package io.qameta.allure.xunit;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeResultAggregator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 05.03.16
 */
public class XunitResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        if (result.findOne(LabelName.PARENT_SUITE).isPresent()) {
            return Arrays.asList(
                    TreeGroup.oneByLabel(result, LabelName.PARENT_SUITE),
                    TreeGroup.oneByLabel(result, LabelName.SUITE)
            );
        }

        return Collections.singletonList(
                TreeGroup.oneByLabel(result, LabelName.SUITE)
        );
    }
}
