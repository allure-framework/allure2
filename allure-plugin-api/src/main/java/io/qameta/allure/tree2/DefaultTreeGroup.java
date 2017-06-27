package io.qameta.allure.tree2;

import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeGroup implements TreeGroup {

    private final String name;

    private final SortedSet<TreeNode> children = new TreeSet<>(
            comparing(TreeNode::getName, nullsFirst(naturalOrder()))
    );

    public DefaultTreeGroup(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SortedSet<TreeNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(final TreeNode node) {
        children.add(node);
    }
}
