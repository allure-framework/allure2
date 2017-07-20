package io.qameta.allure.tree;

import io.qameta.allure.entity.TestResult;

import static io.qameta.allure.tree.TreeUtils.createGroupUid;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTree extends AbstractTree<TestResult, TestResultTreeGroup, TestResultTreeLeaf> {

    public TestResultTree(final String name, final TreeClassifier<TestResult> treeClassifier) {
        this(name, treeClassifier, new TestResultGroupFactory(), new TestResultLeafFactory());
    }

    public TestResultTree(final String name, final TreeClassifier<TestResult> treeClassifier,
                          final TreeGroupFactory<TestResult, TestResultTreeGroup> groupFactory,
                          final TreeLeafFactory<TestResult, TestResultTreeGroup, TestResultTreeLeaf> leafFactory) {
        super(new TestResultTreeGroup(createGroupUid(null, name), name),
                treeClassifier, groupFactory, leafFactory);
    }

    public String getUid() {
        return root.getUid();
    }

    @Override
    protected Class<TestResultTreeGroup> getRootType() {
        return TestResultTreeGroup.class;
    }
}
