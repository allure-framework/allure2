package io.qameta.allure.tree;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeClassifier<T> {

    List<TreeLayer> classify(T item);

}
