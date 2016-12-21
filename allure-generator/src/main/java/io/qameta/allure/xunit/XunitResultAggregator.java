package io.qameta.allure.xunit;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeResultAggregator;
import io.qameta.allure.tree.TreeGroup;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 05.03.16
 */
public class XunitResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return Collections.singletonList(
                TreeGroup.oneByLabel(result, LabelName.SUITE, "Default suite")
        );
    }
}
