package io.qameta.allure.tree2;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface Classifier<T> {

    List<String> classify(T item);

    TreeGroup factory(String name, T item);

}
