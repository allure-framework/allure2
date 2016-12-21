package io.qameta.allure.packages;

import io.qameta.allure.entity.TestCaseResult;
import io.qameta.allure.tree.TreeGroup;
import io.qameta.allure.tree.TreeResultAggregator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author charlie (Dmitry Baev).
 */
public class PackagesResultAggregator extends TreeResultAggregator {

    @Override
    protected List<TreeGroup> getGroups(TestCaseResult result) {
        return result.findOne("package")
                .map(testClass -> Arrays.asList(testClass.split("\\.")))
                .orElse(Collections.singletonList("Without package"))
                .stream()
                .map(TreeGroup::values)
                .collect(Collectors.toList());
    }
}
