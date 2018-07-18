package io.qameta.allure.tree;

/**
 * @param <T> the type of tree items
 * @param <S> the type of tree leafs
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeGroupFactory<T, S extends TreeGroup> {

    S create(S parent, String name, T item);

}
