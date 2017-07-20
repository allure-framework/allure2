package io.qameta.allure.tree;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeGroup extends DefaultTreeGroup {

    private String uid;

    public TestResultTreeGroup(final String uid, final String name) {
        super(name);
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }
}
