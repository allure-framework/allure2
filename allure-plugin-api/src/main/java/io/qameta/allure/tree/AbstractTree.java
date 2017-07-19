package io.qameta.allure.tree;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
public abstract class AbstractTree<T, S extends TreeGroup, U extends TreeLeaf> implements Tree<T> {

    protected final S root;

    private final TreeClassifier<T> treeClassifier;

    private final TreeGroupFactory<T, S> groupFactory;

    private final TreeLeafFactory<T, S, U> leafFactory;

    public AbstractTree(final S root, final TreeClassifier<T> treeClassifier,
                        final TreeGroupFactory<T, S> groupFactory, final TreeLeafFactory<T, S, U> leafFactory) {
        this.root = root;
        this.treeClassifier = treeClassifier;
        this.groupFactory = groupFactory;
        this.leafFactory = leafFactory;
    }

    @Override
    public void add(final T item) {
        getEndNodes(item, root, treeClassifier.classify(item), 0)
                .forEach(node -> {
                    final TreeLeaf leafNode = leafFactory.create(node, item);
                    node.addChild(leafNode);
                });
    }

    protected Stream<S> getEndNodes(final T item, final S node,
                                    final List<TreeLayer> classifiers,
                                    final int index) {
        if (index >= classifiers.size()) {
            return Stream.of(node);
        }
        final TreeLayer layer = classifiers.get(index);
        return layer.getGroupNames().stream()
                .flatMap(name -> {
                    // @formatter:off
                    final S child = node.findNodeOfType(name, getRootType())
                        .orElseGet(() -> {
                            final S created = groupFactory.create(node, name, item);
                            node.addChild(created);
                            return created;
                        });
                    // @formatter:on
                    return getEndNodes(item, child, classifiers, index + 1);
                });
    }

    @Override
    public String getName() {
        return root.getName();
    }

    @Override
    public Set<TreeNode> getChildren() {
        return Collections.unmodifiableSet(root.getChildren());
    }

    @Override
    public void addChild(final TreeNode node) {
        root.addChild(node);
    }

    protected abstract Class<S> getRootType();

}
