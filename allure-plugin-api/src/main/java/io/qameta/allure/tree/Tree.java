package io.qameta.allure.tree;

/**
 * @param <T> the type of tree item.
 * @author charlie (Dmitry Baev).
 */
public interface Tree<T> extends TreeGroup {

    void add(T item);

}
