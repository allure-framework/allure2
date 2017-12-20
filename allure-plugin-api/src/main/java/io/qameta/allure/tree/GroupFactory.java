package io.qameta.allure.tree;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface GroupFactory<T extends GroupNode> {

    T create(T parent, GroupNodeContext context);

}
