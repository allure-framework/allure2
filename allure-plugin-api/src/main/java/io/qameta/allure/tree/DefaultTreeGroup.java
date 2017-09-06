package io.qameta.allure.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeGroup implements TreeGroup {

    private String name;

    private List<TreeNode> children = new ArrayList<>();

    public DefaultTreeGroup(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TreeNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(final TreeNode node) {
        children.add(node);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setChildren(final List<TreeNode> children) {
        this.children = children;
    }
}
