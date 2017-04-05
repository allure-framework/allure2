package io.qameta.allure.packages;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.AbstractTreeAggregator;
import io.qameta.allure.tree.TreeGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The plugin adds packages tab to the report.
 *
 * @since 2.0
 */
public class PackagesPlugin extends AbstractTreeAggregator {

    @Override
    protected List<TreeGroup> getGroups(final TestCaseResult result) {
        final Optional<String> aPackage = result.findOne(LabelName.PACKAGE);
        return aPackage
                .map(testClass -> Arrays.asList(testClass.split("\\.")))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(TreeGroup::values)
                .collect(Collectors.toList());
    }

    @Override
    protected String getNodeName(final TestCaseResult result) {
        return result
                .findOne(LabelName.TEST_METHOD)
                .filter(method -> !method.isEmpty())
                .orElseGet(result::getName);
    }

    @Override
    protected String getFileName() {
        return "packages.json";
    }
}
