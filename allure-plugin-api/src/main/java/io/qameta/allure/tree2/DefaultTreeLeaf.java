package io.qameta.allure.tree2;

/**
 * @author charlie (Dmitry Baev).
 */
public class DefaultTreeLeaf implements TreeLeaf {

    private final String name;

    public DefaultTreeLeaf(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
