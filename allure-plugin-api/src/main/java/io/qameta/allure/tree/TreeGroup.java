package io.qameta.allure.tree;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TreeGroup extends TreeNode {

    Set<TreeNode> getChildren();

    <T extends TreeNode> void addChild(T node);

    default TreeNode computeIfAbsent(String name, Function<String, ? extends TreeGroup> mappingFunction) {
        return getChildren().stream()
                .filter(node -> Objects.equals(node.getName(), name))
                .findFirst()
                .orElseGet(() -> {
                    final TreeGroup child = mappingFunction.apply(name);
                    addChild(child);
                    return child;
                });
    }

}
