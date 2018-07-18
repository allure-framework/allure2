package io.qameta.allure.tree;

/**
 * @param <T> the type of tree items
 * @param <S> the type of tree groups
 * @param <U> the type of tree leafs
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeLeafFactory<T, S extends TreeGroup, U extends TreeLeaf> {

    U create(S parent, T item);

}
