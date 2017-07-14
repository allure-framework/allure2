package io.qameta.allure.tree;

import io.qameta.allure.entity.TestResult;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTree extends AbstractTree<TestResult, TestResultTreeGroup, TestResultTreeLeaf> {

    public TestResultTree(final String name, final TreeClassifier<TestResult> treeClassifier) {
        this(name, treeClassifier, new TestResultGroupFactory(), new TestResultLeafFactory());
    }

    public TestResultTree(final String name, final TreeClassifier<TestResult> treeClassifier,
                          final TreeGroupFactory<TestResult, TestResultTreeGroup> groupFactory,
                          final TreeLeafFactory<TestResult, TestResultTreeLeaf> leafFactory) {
        super(new TestResultTreeGroup(name, name),
                treeClassifier, groupFactory, leafFactory);
    }

    @Override
    protected Class<TestResultTreeGroup> getRootType() {
        return TestResultTreeGroup.class;
    }
}
