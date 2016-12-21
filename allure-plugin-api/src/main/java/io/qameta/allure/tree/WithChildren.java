package io.qameta.allure.tree;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public interface WithChildren {

    List<TreeNode> getChildren();

}
