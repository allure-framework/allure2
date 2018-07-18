package io.qameta.allure.tree;

import java.util.List;

/**
 * @param <T> the type of classified items.
 * @author charlie (Dmitry Baev).
 */
@FunctionalInterface
public interface TreeClassifier<T> {

    List<TreeLayer> classify(T item);

}
