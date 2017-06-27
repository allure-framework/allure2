package io.qameta.allure.tree2;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Tree<T> extends TreeGroup {

    void add(T item);

}
