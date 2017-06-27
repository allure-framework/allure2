package io.qameta.allure.tree2;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTree<T> extends DefaultTreeGroup implements Tree<T> {

    private final Function<T, List<Classifier<T>>> classifiersFunction;

    private final Function<T, Optional<? extends TreeLeaf>> leafFunction;

    public DefaultTree(final String name, final Function<T, List<Classifier<T>>> classifiersFunction,
                       final Function<T, Optional<? extends TreeLeaf>> leafFunction) {
        super(name);
        this.classifiersFunction = classifiersFunction;
        this.leafFunction = leafFunction;
    }

    @Override
    public void add(final T item) {
        final Optional<? extends TreeLeaf> leaf = leafFunction.apply(item);
        if (!leaf.isPresent()) {
            //no need to add a node for such item;
            return;
        }
        final TreeLeaf leafNode = leaf.get();

        final List<Classifier<T>> classifiers = classifiersFunction.apply(item);
        getEndNodes(item, this, classifiers, 0)
                .forEach(node -> node.addChild(leafNode));
    }

    protected Stream<TreeGroup> getEndNodes(final T item, final TreeGroup node, final List<Classifier<T>> classifiers,
                                            final int index) {
        if (index >= classifiers.size()) {
            return Stream.of(node);
        }
        final Classifier<T> classifier = classifiers.get(index);
        return classifier.classify(item).stream()
                .flatMap(name -> {
                    final TreeGroup child = (TreeGroup) node.computeIfAbsent(name, key -> classifier.factory(key, item));
                    node.addChild(child);
                    return getEndNodes(item, child, classifiers, index + 1);
                });
    }
}
