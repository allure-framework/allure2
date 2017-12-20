package io.qameta.allure.tree;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
@SuppressWarnings("InterfaceTypeParameterName")
public interface GroupNode<LEAF extends LeafNode, GROUP extends GroupNode<LEAF, GROUP>> extends Node {

    String getUid();

    GroupNodeContext getContext();

    List<GROUP> getGroups();

    List<LEAF> getLeafs();

}
