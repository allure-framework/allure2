package io.qameta.allure.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("checkstyle:all")
public abstract class AbstractTree<ITEM, LEAF extends LeafNode, GROUP extends GroupNode<LEAF, GROUP>>
        implements GroupNode<LEAF, GROUP> {

    private final GROUP root;

    private final Classifier<ITEM> classifier;

    private final GroupFactory<GROUP> groupFactory;
    private final LeafFactory<ITEM, LEAF, GROUP> leafFactory;

    private long total = -1;
    private long shown = 0;

    protected AbstractTree(final GROUP root,
                           final Classifier<ITEM> classifier,
                           final GroupFactory<GROUP> groupFactory,
                           final LeafFactory<ITEM, LEAF, GROUP> leafFactory) {
        this.root = root;
        this.classifier = classifier;
        this.groupFactory = groupFactory;
        this.leafFactory = leafFactory;
    }

    public List<GROUP> getPathForNode(final String groupNodeUid) {
        return getPathForNode(root, groupNodeUid);
    }

    private List<GROUP> getPathForNode(final GROUP node, final String groupNodeUid) {
        if (Objects.equals(node.getUid(), groupNodeUid)) {
            return Collections.singletonList(node);
        }
        return node.getGroups().stream()
                .map(group -> getPathForNode(group, groupNodeUid))
                .filter(Objects::nonNull)
                .findAny()
                .map(groups -> {
                    final List<GROUP> path = new ArrayList<>();
                    path.add(node);
                    path.addAll(groups);
                    return path;
                })
                .orElse(null);
    }

    public void add(ITEM item) {
        shown++;
        final List<Layer> treeLayers = classifier.classify(item);
        getEndNodes(root, treeLayers, 0)
                .forEach(group -> {
                    final LEAF leaf = leafFactory.create(group, item);
                    group.getLeafs().add(leaf);
                });
    }

    protected Stream<GROUP> getEndNodes(final GROUP node,
                                        final List<Layer> layers,
                                        final int index) {
        if (index >= layers.size()) {
            return Stream.of(node);
        }
        final Layer layer = layers.get(index);
        return layer.getGroups().stream()
                .flatMap(context -> {
                    final GROUP groupNode = node.getGroups().stream()
                            .filter(group -> Objects.equals(group.getContext(), context))
                            .findAny()
                            .orElseGet(() -> {
                                final GROUP newGroup = groupFactory.create(node, context);
                                node.getGroups().add(newGroup);
                                return newGroup;
                            });
                    return getEndNodes(groupNode, layers, index + 1);
                });
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(final long total) {
        this.total = total;
    }

    public long getShown() {
        return shown;
    }

    @Override
    public GroupNodeContext getContext() {
        return root.getContext();
    }

    @Override
    public List<GROUP> getGroups() {
        return root.getGroups();
    }

    @Override
    public List<LEAF> getLeafs() {
        return root.getLeafs();
    }

    @Override
    public String getName() {
        return root.getName();
    }

    @Override
    public String getUid() {
        return root.getUid();
    }
}
