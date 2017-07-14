package io.qameta.allure.tree;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeLeafFactory<T, S extends TreeLeaf> {

    S create(TreeGroup parent, T item);

}
