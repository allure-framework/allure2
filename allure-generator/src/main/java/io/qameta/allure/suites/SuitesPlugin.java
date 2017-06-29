package io.qameta.allure.suites;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.AbstractTreeAggregator;
import io.qameta.allure.tree.TreeGroup;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin that generates data for Suites tab.
 *
 * @since 2.0
 */
public class SuitesPlugin extends AbstractTreeAggregator {

    @Override
    protected String getFileName() {
        return "suites.json";
    }

    @Override
    protected List<TreeGroup> getGroups(final TestResult result) {
        return Stream.of(LabelName.PARENT_SUITE, LabelName.SUITE, LabelName.SUB_SUITE)
                .map(result::findOne)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TreeGroup::values)
                .collect(Collectors.toList());
    }
}
