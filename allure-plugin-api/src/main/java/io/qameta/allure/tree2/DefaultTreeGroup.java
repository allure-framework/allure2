package io.qameta.allure.tree2;

import java.util.HashSet;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeGroup implements TreeGroup {

    private String name;

    private Set<TreeNode> children = new HashSet<>();

    public DefaultTreeGroup(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<TreeNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(final TreeNode node) {
        children.add(node);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setChildren(final Set<TreeNode> children) {
        this.children = children;
    }
}
