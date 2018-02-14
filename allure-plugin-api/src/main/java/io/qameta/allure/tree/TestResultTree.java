package io.qameta.allure.tree;

import io.qameta.allure.entity.TestResult;

import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTree extends AbstractTree<TestResult, TestResultLeafNode, TestResultGroupNode> {

    public TestResultTree(final String name, final Classifier<TestResult> classifier) {
        super(
                new TestResultGroupNode(null, new GroupNodeContext(null, name)),
                classifier,
                getGroupFactory(),
                getLeafFactory()
        );
    }

    @Override
    public List<TestResultGroupNode> getGroups() {
        return super.getGroups();
    }

    @Override
    public List<TestResultLeafNode> getLeafs() {
        return super.getLeafs();
    }

    private static LeafFactory<TestResult, TestResultLeafNode, TestResultGroupNode> getLeafFactory() {
        return (parent, testResult) -> new TestResultLeafNode(parent.getUid(), testResult);
    }

    private static GroupFactory<TestResultGroupNode> getGroupFactory() {
        return (parent, id) -> new TestResultGroupNode(parent.getUid(), id);
    }
}
