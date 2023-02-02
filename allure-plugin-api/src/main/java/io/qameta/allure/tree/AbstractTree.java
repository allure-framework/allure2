/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.tree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @param <T> the type of accepted items
 * @param <S> the type of tree group
 * @param <U> the type of tree leaf
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
    public List<TreeNode> getChildren() {
        return Collections.unmodifiableList(root.getChildren());
    }

    @Override
    public void addChild(final TreeNode node) {
        root.addChild(node);
    }

    protected abstract Class<S> getRootType();

}
