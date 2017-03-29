package io.qameta.allure.packages;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeAggregator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class PackagesResultAggregator extends TreeAggregator {

    @Override
    protected List<TreeGroup> getGroups(final TestCaseResult result) {
        final Optional<String> aPackage = result.findOne(LabelName.PACKAGE);
        if (!aPackage.isPresent()) {
            return Collections.emptyList();
        }
        return aPackage
                .map(testClass -> Arrays.asList(testClass.split("\\.")))
                .get()
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
}
