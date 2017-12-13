package io.qameta.allure.tree;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Tree<T> extends TreeGroup {

    void add(T item);

}
