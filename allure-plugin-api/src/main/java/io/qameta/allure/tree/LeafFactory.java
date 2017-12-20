package io.qameta.allure.tree;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("InterfaceTypeParameterName")
@FunctionalInterface
public interface LeafFactory<ITEM, LEAF extends LeafNode, GROUP extends GroupNode<LEAF, GROUP>> {

    LEAF create(GROUP parent, ITEM item);

}
