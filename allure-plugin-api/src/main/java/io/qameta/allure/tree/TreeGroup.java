package io.qameta.allure.tree;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TreeGroup extends TreeNode {

    Set<TreeNode> getChildren();

    void addChild(TreeNode node);

    default <T extends TreeNode> Optional<T> findNodeOfType(final String name, final Class<T> type) {
        return getChildren().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .filter(node -> Objects.equals(node.getName(), name))
                .findFirst();
    }

}
