package io.qameta.allure.tree2;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeGroup extends DefaultTreeGroup {

    private final String uid;

    public TestResultTreeGroup(final String name, final String uid) {
        super(name);
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}
