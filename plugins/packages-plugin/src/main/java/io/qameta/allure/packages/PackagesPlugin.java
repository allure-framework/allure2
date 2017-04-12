package io.qameta.allure.packages;

import io.qameta.allure.entity.LabelName;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.AbstractTreeAggregator;
import io.qameta.allure.tree.TestGroupNode;
import io.qameta.allure.tree.TreeData;
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
    protected List<TreeGroup> getGroups(final TestResult result) {
        final Optional<String> aPackage = result.findOne(LabelName.PACKAGE);
        return aPackage
                .map(testClass -> Arrays.asList(testClass.split("\\.")))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(TreeGroup::values)
                .collect(Collectors.toList());
    }

    @Override
    protected String getNodeName(final TestResult result) {
        return result
                .findOne(LabelName.TEST_METHOD)
                .filter(method -> !method.isEmpty())
                .orElseGet(result::getName);
    }

    @Override
    protected String getFileName() {
        return "packages.json";
    }

    @Override
    protected TreeData postProcess(final TreeData treeData) {
        treeData.getChildren().stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .forEach(this::collapseGroupsWithOnlyOneChild);
        return treeData;
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    protected void collapseGroupsWithOnlyOneChild(final TestGroupNode groupNode) {
        groupNode.getChildren().stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .forEach(this::collapseGroupsWithOnlyOneChild);

        final long count = groupNode.getChildren().stream()
                .filter(TestGroupNode.class::isInstance)
                .count();

        if (count == 1) {
            groupNode.getChildren().stream()
                    .filter(TestGroupNode.class::isInstance)
                    .map(TestGroupNode.class::cast)
                    .findAny()
                    .ifPresent(next -> {
                        groupNode.setName(getName(groupNode, next));
                        groupNode.setChildren(next.getChildren());
                    });
        }
    }

    protected String getName(final TestGroupNode parent, final TestGroupNode child) {
        return String.format("%s.%s", parent.getName(), child.getName());
    }

}
