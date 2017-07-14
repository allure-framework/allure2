package io.qameta.allure.tree;

import io.qameta.allure.entity.TestResult;

import static io.qameta.allure.tree.TreeUtils.createGroupUid;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultGroupFactory implements TreeGroupFactory<TestResult, TestResultTreeGroup> {

    @Override
    public TestResultTreeGroup create(final TestResultTreeGroup parent, final String name, final TestResult item) {
        return new TestResultTreeGroup(createGroupUid(parent.getUid(), name), name);
    }
}
