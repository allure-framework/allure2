package io.qameta.allure.tree;

import io.qameta.allure.Finalizer;

/**
 * @author charlie (Dmitry Baev).
 */
public class TreeCollapseGroupsWithOneChildFinalizer implements Finalizer<TreeData> {

    @Override
    public Object convert(TreeData treeData) {
        treeData.getChildren().stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .forEach(this::collapseGroupsWithOnlyOneChild);
        return treeData;
    }

    protected void collapseGroupsWithOnlyOneChild(TestGroupNode groupNode) {
        groupNode.getChildren().stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .forEach(this::collapseGroupsWithOnlyOneChild);

        long count = groupNode.getChildren().stream()
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

    protected String getName(TestGroupNode parent, TestGroupNode child) {
        return String.format("%s.%s", parent.getName(), child.getName());
    }

}
