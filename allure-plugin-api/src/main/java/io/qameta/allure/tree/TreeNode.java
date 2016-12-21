package io.qameta.allure.tree;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author charlie (Dmitry Baev).
 */
public interface TreeNode {

    String getUid();

    String getName();

    @XmlElement
    default String getType() {
        return getClass().getSimpleName();
    }

}
