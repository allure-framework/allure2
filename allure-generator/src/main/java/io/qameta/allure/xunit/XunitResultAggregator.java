package io.qameta.allure.xunit;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeResultAggregator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 05.03.16
 */
public class XunitResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(final TestCaseResult result) {
        return Stream.of(LabelName.PARENT_SUITE, LabelName.SUITE, LabelName.SUB_SUITE)
                .map(result::findOne)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TreeGroup::values)
                .collect(Collectors.toList());
    }
}
