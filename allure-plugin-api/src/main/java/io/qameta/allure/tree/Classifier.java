package io.qameta.allure.tree;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface Classifier<T> {

    List<Layer> classify(T item);

}
