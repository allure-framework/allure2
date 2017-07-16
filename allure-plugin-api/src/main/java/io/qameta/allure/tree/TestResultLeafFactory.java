package io.qameta.allure.tree;

import io.qameta.allure.entity.TestResult;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultLeafFactory implements TreeLeafFactory<TestResult, TestResultTreeGroup, TestResultTreeLeaf> {

    @Override
    public TestResultTreeLeaf create(final TestResultTreeGroup parent, final TestResult item) {
        return new TestResultTreeLeaf(parent.getUid(), item);
    }

}
