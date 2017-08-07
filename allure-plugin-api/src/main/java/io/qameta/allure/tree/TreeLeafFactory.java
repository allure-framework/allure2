package io.qameta.allure.tree;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeLeafFactory<T, S extends TreeGroup, U extends TreeLeaf> {

    U create(S parent, T item);

}
