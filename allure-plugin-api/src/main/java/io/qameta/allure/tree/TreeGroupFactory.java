package io.qameta.allure.tree;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeGroupFactory<T, S extends TreeGroup> {

    S create(S parent, String name, T item);

}
